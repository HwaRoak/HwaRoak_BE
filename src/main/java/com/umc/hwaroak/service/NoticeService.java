package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.NoticeResponseDto;

import java.util.List;

public interface NoticeService {

    List<NoticeResponseDto.PreviewDto> getNoticeList();

    NoticeResponseDto.InfoDto getNoticeDetail(Long id);
}