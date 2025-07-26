package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DiaryService {

    DiaryResponseDto.CreateDto createDiary(DiaryRequestDto requestDto);
    DiaryResponseDto.ThumbnailDto readDiary(LocalDateTime date);
    DiaryResponseDto.CreateDto updateDiary(Long diaryId, DiaryRequestDto requestDto);
    List<DiaryResponseDto.ThumbnailDto> readMonthDiary(Integer year, Integer month);
    void deleteDiary(Long diaryId);
    DiaryResponseDto.DetailDto readDiaryWithDetail(Long diaryId);
}
