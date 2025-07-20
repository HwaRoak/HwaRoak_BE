package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.converter.DiaryConverter;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.DiaryService;
import com.umc.hwaroak.util.OpenAiUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final MemberRepository memberRepository;

    @Transactional
    public DiaryResponseDto createDiary(DiaryRequestDto requestDto) {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info(requestDto.getContent());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        if (diaryRepository.findByRecordDate(memberId, requestDto.getRecordDate()).isPresent()) {
            log.info("{} 날짜의 일기 발견...", requestDto.getRecordDate());
            throw new GeneralException(ErrorCode.DIARY_ALREADY_RECORDED);
        }

        // 요청 감정의 개수 확인하기
        if (requestDto.getEmotionList().size() > 3) {
            throw new GeneralException(ErrorCode.TOO_MANY_EMOTIONS);
        }

        Diary diary = DiaryConverter.toDiary(member, requestDto);
        log.info("작성 일기 내용: " + requestDto.getContent());
        diary.setFeedback(openAiUtil.reviewDiary(diary.getContent()));
        diaryRepository.save(diary);

        // TODO: Reward 계산
        return DiaryConverter.toDto(diary);
    }

    @Transactional(readOnly = true)
    public DiaryResponseDto readDiary(LocalDate date) {

        Long memberId = memberLoader.getCurrentMemberId();

        return DiaryConverter.toDto(diaryRepository.findByRecordDate(memberId, date)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND))
        );
    }

    @Transactional(readOnly = true)
    public DiaryResponseDto.DetailDto readDiaryWithDetail(Long diaryId) {

        memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryConverter.toDetailDto(diary);
    }

    @Transactional
    public DiaryResponseDto updateDiary(Long diaryId, DiaryRequestDto requestDto) {

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

        return DiaryConverter.toDto(diary);
    }

    // 월별 일기 전체 조회하기
    @Transactional(readOnly = true)
    public List<DiaryResponseDto> readMonthDiary(Integer year, Integer month) {

        Long memberId = memberLoader.getCurrentMemberId();
        return diaryRepository.findDiaryByMonth(memberId, year, month);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {

        memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        diaryRepository.delete(diary);
    }
}
