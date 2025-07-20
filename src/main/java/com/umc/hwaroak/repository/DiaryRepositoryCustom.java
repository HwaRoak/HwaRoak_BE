package com.umc.hwaroak.repository;

import com.umc.hwaroak.dto.response.DiaryResponseDto;

import java.util.List;

public interface DiaryRepositoryCustom {

    List<DiaryResponseDto> findDiaryByMonth(Long memberId, Integer year, Integer month);

}
