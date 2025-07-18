package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface DiaryService {

    DiaryResponseDto createDiary(Long memberId, DiaryRequestDto requestDto);
    DiaryResponseDto readDiary(LocalDate date);
    DiaryResponseDto updateDiary(Long diaryId, DiaryRequestDto requestDto);
    List<DiaryResponseDto> readMonthDiary(Long memberId, Integer month);
    void moveToTrash(Long diaryId);
    void cancelDeleteDiary(Long diaryId);
}
