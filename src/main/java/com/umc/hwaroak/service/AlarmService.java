package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.AlarmResponseDto;

import java.util.List;

public interface AlarmService {
    /**
     * 공지 최신 순으로 가져오기
     */
    List<AlarmResponseDto.PreviewDto> getNoticeList();

    /**
     * 공지 상세보기
     */
    AlarmResponseDto.InfoDto getNoticeDetail(Long id);
}
