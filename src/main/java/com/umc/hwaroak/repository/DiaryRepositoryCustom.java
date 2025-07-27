package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.dto.response.DiaryResponseDto;

import java.util.List;

public interface DiaryRepositoryCustom {

    List<DiaryResponseDto.ThumbnailDto> findDiaryByMonth(Long memberId, Integer year, Integer month);

    // 특정 연도와 월의 모든 일기를 조회
    List<Diary> findAllDiariesByYearMonth(Long memberId, int year, int month);

}
