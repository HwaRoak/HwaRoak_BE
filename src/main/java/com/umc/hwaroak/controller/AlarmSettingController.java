package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.AlarmSettingResponseDto;
import com.umc.hwaroak.service.AlarmSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/alarmSetting")
@RequiredArgsConstructor
@Tag(name = "AlarmSetting API", description = "알림 설정 관련 API")
public class AlarmSettingController {

    private final AlarmSettingService alarmSettingService;

    @GetMapping("")
    @Operation(summary = "알림 설정 조회", description = "사용자의 알림 관련 설정들을 조회합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AlarmSettingResponseDto.InfoDto.class)))
    public AlarmSettingResponseDto.InfoDto getAlarmSettingInfo() {
        return alarmSettingService.getAlarmSettingInfo();
    }
}
