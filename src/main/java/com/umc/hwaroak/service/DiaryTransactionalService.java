package com.umc.hwaroak.service;

import com.umc.hwaroak.converter.DiaryConverter;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.infrastructure.transaction.CustomTransactionSynchronization;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.util.OpenAiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryTransactionalService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final OpenAiUtil openAiUtil;
    private final EmotionSummaryService emotionSummaryService;

    @Transactional
    public Diary createDiaryTransactional(Member member, DiaryRequestDto.CreateDto requestDto) {
        log.info("createDiaryTransactional 진입 - member={}, date={}", member.getId(), requestDto.getRecordDate());

        Diary diary = DiaryConverter.toDiary(member, requestDto);
        log.info("작성 일기 내용: {}", requestDto.getContent());
        diary.setFeedback(openAiUtil.reviewDiary(diary.getContent()));

        diaryRepository.save(diary);
        member.setReward((member.getReward() - 1) == 0 ? 7 : (member.getReward() - 1));
        memberRepository.save(member);

        // 커밋 후 월간 요약 업데이트 예약
        final LocalDate targetDate = diary.getRecordDate();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new CustomTransactionSynchronization() {
                @Override public void afterCommit() {
                    log.info("afterCommit 호출; 월간 감정분석 업데이트 시작 - targetDate={}", targetDate);
                    emotionSummaryService.updateMonthlyEmotionSummary(targetDate);
                }
            });
        } else {
            log.warn("트랜잭션 동기화 비활성 -> 즉시 실행 - targetDate={}", targetDate);
            emotionSummaryService.updateMonthlyEmotionSummary(targetDate);
        }

        return diary;
    }
}
