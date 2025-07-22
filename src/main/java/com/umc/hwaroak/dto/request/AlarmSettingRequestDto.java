package com.umc.hwaroak.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalTime;

public class AlarmSettingRequestDto {

    @Getter
    @Schema(description = "알림 설정 변경 요청 DTO")
    public static class EditDto{
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
