package com.umc.hwaroak.repository;

import com.umc.hwaroak.dto.response.DiaryResponseDto;

import java.util.List;

public interface DiaryRepositoryCustom {

    List<DiaryResponseDto.ThumbnailDto> findDiaryByMonth(Long memberId, Integer year, Integer month);

}
