package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import com.umc.hwaroak.dto.response.MainMessageResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface DiaryService {

    DiaryResponseDto.CreateDto createDiary(DiaryRequestDto requestDto);
    DiaryResponseDto.ThumbnailDto readDiary(LocalDate date);
    DiaryResponseDto.CreateDto updateDiary(Long diaryId, DiaryRequestDto requestDto);
    List<DiaryResponseDto.ThumbnailDto> readMonthDiary(Integer year, Integer month);
    void deleteDiary(Long diaryId);
    DiaryResponseDto.DetailDto readDiaryWithDetail(Long diaryId);
    boolean isRewardAvailable(Member member);
    MainMessageResponseDto claimReward();
}
