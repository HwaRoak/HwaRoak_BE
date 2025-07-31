package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

public class AlarmSettingResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AlarmSettingResponseDto", description = "알림 설정 조회 응답 DTO")
    public static class InfoDto{

        @Schema(description = "리마인더 알림 허용 여부", example = "true")
        private Boolean reminderEnabled;

        @Schema(description = "리마인더 설정 시간", example = "21:30")
        private LocalTime reminderTime;

        @Schema(description = "불씨 알림 허용 여부", example = "true")
        private Boolean fireAlarmEnabled;

        @Schema(description = "모든 알림 끄기 여부", example = "true")
        private Boolean allOffEnabled;

    }
}
