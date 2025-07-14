package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.dto.NoticeResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;

    /**
     * 공지(NOTIFIACTION) 최신순 정렬 가져오기
     */
    @Override
    public List<NoticeResponseDto.PreviewDto> getNoticeList() {
        return alarmRepository.findByAlarmTypeOrderByCreatedAtDesc(AlarmType.NOTIFICATION).stream()
                .map(alarm -> NoticeResponseDto.PreviewDto.builder()
                        .id(alarm.getId())
                        .title(alarm.getTitle())
                        .createdAt(alarm.getCreatedAt())
                        .build())
                .collect(toList());
    }

    /**
     * 공지 id로 상세조회하기
     */
    @Override
    public NoticeResponseDto.InfoDto getNoticeDetail(Long id) {
        Alarm alarm = alarmRepository.findByIdAndAlarmType(id, AlarmType.NOTIFICATION)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOTICE_NOT_FOUND));

        return NoticeResponseDto.InfoDto.builder()
                .id(alarm.getId())
                .title(alarm.getTitle())
                .content(alarm.getContent())
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
