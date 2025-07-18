package com.umc.hwaroak.service;

import com.umc.hwaroak.converter.DiaryConverter;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.util.OpenAiUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryServiceImpl implements DiaryService {

    private final OpenAiUtil openAiUtil;

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public DiaryResponseDto createDiary(
            Long memberId, DiaryRequestDto requestDto) { // TODO: SpringSecurity 기반으로 변경

        log.info(requestDto.getContent());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        if (diaryRepository.findByRecordDate(requestDto.getRecordDate()).isPresent()) {
            log.info("{} 날짜의 일기 발견...", requestDto.getRecordDate());
            throw new GeneralException(ErrorCode.DIARY_ALREADY_RECORDED);
        }

        Diary diary = DiaryConverter.toDiary(member, requestDto);
        log.info(requestDto.getContent());
        diary.setFeedback(openAiUtil.reviewDiary(diary.getContent()));
        diary.setDeleted(false);
        diaryRepository.save(diary);

        // TODO: Reward 계산
        return DiaryConverter.toDto(diary);
    }

    @Transactional(readOnly = true)
    public DiaryResponseDto readDiary(LocalDate date) {

        return DiaryConverter.toDto(diaryRepository.findByRecordDate(date)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND))
        );
    }

    @Transactional
    public DiaryResponseDto updateDiary(Long diaryId, DiaryRequestDto requestDto) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        diary.update(requestDto.getContent(), requestDto.getEmotion());
        diary.setFeedback(openAiUtil.reviewDiary(requestDto.getContent()));
        diaryRepository.save(diary);

        return DiaryConverter.toDto(diary);
    }

    // 월별 일기 전체 조회하기
    @Transactional(readOnly = true)
    public List<DiaryResponseDto> readMonthDiary(
            Long memberId,   // TODO: Spring Security 기반으로 변경
            Integer month) {
        return diaryRepository.findDiaryByMonth(memberId, month);
    }

    @Transactional
    public void moveToTrash(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        diary.setDeleted(true);
        diary.setDeletedAt(LocalDate.now());
        diaryRepository.save(diary);
    }

    @Override
    @Transactional
    public void cancelDeleteDiary(Long diaryId) {

    }
}
