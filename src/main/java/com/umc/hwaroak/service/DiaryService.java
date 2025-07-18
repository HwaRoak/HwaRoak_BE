package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;

import java.time.LocalDate;

public interface DiaryService {

    DiaryResponseDto createDiary(DiaryRequestDto requestDto);
    DiaryResponseDto readDiary(LocalDate date);
    DiaryResponseDto updateDiary(Long diaryId, DiaryRequestDto requestDto);
}
