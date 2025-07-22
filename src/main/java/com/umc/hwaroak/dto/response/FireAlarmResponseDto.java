package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FireAlarmResponseDto {

    @Schema(description = "알림 생성 시각", example = "2025-07-11T12:00:00Z")
    private String notifiedAt;

    @Schema(description = "알림 메시지", example = "다음 알림은 59분 후에 전송돼요!")
    private String message;
}
