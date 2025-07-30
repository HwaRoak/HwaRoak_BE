package com.umc.hwaroak.converter;

import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.dto.response.AlarmResponseDto;

public class AlarmConverter {

    public static AlarmResponseDto.PreviewDto toPreviewDto(Alarm alarm) {
        return AlarmResponseDto.PreviewDto.builder()
                .id(alarm.getId())
                .receiverId(alarm.getReceiver() != null ? alarm.getReceiver().getId() : null)
                .alarmType(alarm.getAlarmType())
                .title(alarm.getTitle())
                .message(alarm.getMessage())
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
