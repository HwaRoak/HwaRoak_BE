package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Notice;
import com.umc.hwaroak.dto.NoticeResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.NoticeRepository;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Override
    public List<NoticeResponseDto.PreviewDto> getNoticeList() {
        return noticeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(notice -> NoticeResponseDto.PreviewDto.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .createdAt(notice.getCreatedAt())
                        .build())
                .collect(toList());
    }

    @Override
    public NoticeResponseDto.InfoDto getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOTICE_NOT_FOUND));

        return NoticeResponseDto.InfoDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
