package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.AlarmResponseDto;
import com.umc.hwaroak.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alarm", description = "알림 관련 API")
@RestController
@RequestMapping("/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "공지 목록 조회", description = "alarmType = NOTIFICATION 인 공지를 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 목록 조회 성공")
    @GetMapping("/notices")
    public List<AlarmResponseDto.PreviewDto> getNoticeList() {
        return alarmService.getNoticeList();
    }

    @Operation(summary = "공지 상세 조회", description = "alarmType = NOTIFICATION 인 공지의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 상세 조회 성공")
    @ApiResponse(responseCode = "4041", description = "공지 없음 (NOTICE_NOT_FOUND)")
    @GetMapping("/{id}")
    public AlarmResponseDto.InfoDto getNoticeDetail(@PathVariable Long id) {
        return alarmService.getNoticeDetail(id);
    }
}
