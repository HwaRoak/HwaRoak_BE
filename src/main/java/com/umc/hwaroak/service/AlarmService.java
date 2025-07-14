package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.NoticeResponseDto;

import java.util.List;

public interface AlarmService {
    /**
     * 공지 최신 순으로 가져오기
     */
    List<NoticeResponseDto.PreviewDto> getNoticeList();

    /**
     * 공지 상세보기
     */
    NoticeResponseDto.InfoDto getNoticeDetail(Long id);
}
