package com.umc.hwaroak.service.serviceImpl;

import com.umc.hwaroak.event.ItemUpdateEvent;
import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.converter.DiaryConverter;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import com.umc.hwaroak.event.ItemRollbackEvent;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.infrastructure.transaction.CustomTransactionSynchronization;
import com.umc.hwaroak.lock.DiaryLockTemplate;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.MemberItemRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.DiaryService;
import com.umc.hwaroak.service.DiaryTransactionalService;
import com.umc.hwaroak.service.EmotionSummaryService;
import com.umc.hwaroak.util.OpenAiUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryServiceImpl implements DiaryService {

    private final OpenAiUtil openAiUtil;
    private final MemberLoader memberLoader;

    private final DiaryRepository diaryRepository;
    private final DiaryLockTemplate diaryLockTemplate;
    private final DiaryTransactionalService diaryTransactionalService;

    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final EmotionSummaryService emotionSummaryService;
    private final MemberItemRepository memberItemRepository;

    @Transactional
    public DiaryResponseDto.CreateDto createDiary(DiaryRequestDto.CreateDto requestDto) {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info(requestDto.getContent());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // 요청 감정의 개수 확인하기
        if (requestDto.getEmotionList().size() > 3) {
            throw new GeneralException(ErrorCode.TOO_MANY_EMOTIONS);
        }

        // LOCK 이전에 DB 조회 시작
        if (diaryRepository.findByRecordDate(member.getId(), requestDto.getRecordDate()).isPresent()) {
            log.info("{} 날짜의 일기 발견...", requestDto.getRecordDate());
            throw new GeneralException(ErrorCode.DIARY_ALREADY_RECORDED);
        }

        // LOCK
        String lockName = "diary:" + member.getId() + ":" + requestDto.getRecordDate();
        // 락 획득 후 Transactional 시행
        Diary diary = diaryLockTemplate.executeWithLock(lockName, () ->
            // 저장
            diaryTransactionalService.createDiaryTransactional(member, requestDto) // 프록시 경유 호출
        );

        // 아이템 획득 이벤트 발행
        if (member.getReward() ==7) {
            log.info("새로운 아이템 수령 가능 대상으로 등록...");
            eventPublisher.publishEvent(new ItemUpdateEvent(this, member.getId()));
        }

        String nextItemName = memberItemRepository.nextItemName(member);

        return DiaryConverter.toCreateDto(diary, nextItemName);
    }

    @Transactional(readOnly = true)
    public DiaryResponseDto.ThumbnailDto readDiary(LocalDate date) {

        Member member = memberLoader.getMemberByContextHolder();

        return DiaryConverter.toThumbnailDto(diaryRepository.findByRecordDate(member.getId(), date)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public DiaryResponseDto.DetailDto readDiaryWithDetail(Long diaryId) {

        memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryConverter.toDetailDto(diary);
    }

    @Transactional
    public DiaryResponseDto.CreateDto updateDiary(Long diaryId, DiaryRequestDto.CreateDto requestDto) {

        Member member = memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        List<Emotion> emotionList = requestDto.getEmotionList().stream()
                        .map(Emotion::fromDisplayName)
                                .collect(Collectors.toList());

        // 요청 감정의 개수 확인하기
        if (emotionList.size() > 3) {
            throw new GeneralException(ErrorCode.TOO_MANY_EMOTIONS);
        }

        diary.update(requestDto.getContent(), emotionList);
        diary.setFeedback(openAiUtil.reviewDiary(requestDto.getContent()));
        diaryRepository.save(diary);

        // 커밋 후에만 월간 요약 업데이트하도록 예약
        final LocalDate targetDate = diary.getRecordDate();
        updateAfterCommit(targetDate);

        String nextItemName = memberItemRepository.nextItemName(member);
        return DiaryConverter.toCreateDto(diary, nextItemName);
    }

    // 월별 일기 전체 조회하기
    @Transactional(readOnly = true)
    public List<DiaryResponseDto.ThumbnailDto> readMonthDiary(Integer year, Integer month) {

        Long memberId = memberLoader.getCurrentMemberId();
        return diaryRepository.findDiaryByMonth(memberId, year, month);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {

        Member member = memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        long diaryCnt = diaryRepository.countByMemberId(member.getId());
        // member Reward 수정
        int reward = member.getReward();

        if (reward == 7) {
            // 이전의 아이템 상태로 돌아가기
            eventPublisher.publishEvent(new ItemRollbackEvent(this, member.getId()));
            member.setReward(1);
        }
        else {
            member.setReward(reward + 1);
        }

        memberRepository.save(member); // 변경사항 저장
        diaryRepository.delete(diary);

        // 커밋 후에만 월간 요약 업데이트하도록 예약
        final LocalDate targetDate = diary.getRecordDate();
        updateAfterCommit(targetDate);
    }

    private void updateAfterCommit(LocalDate targetDate) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new CustomTransactionSynchronization() {
                @Override public void afterCommit() {
                    log.debug("afterCommit 호출; 월간 감정분석 업데이트 시작 - targetDate={}", targetDate);
                    emotionSummaryService.updateMonthlyEmotionSummary(targetDate);
                }
            });
        } else {
            log.warn("트랜잭션 동기화 비활성 -> 즉시 실행 - targetDate={}", targetDate);
            emotionSummaryService.updateMonthlyEmotionSummary(targetDate);
        }
    }
}
