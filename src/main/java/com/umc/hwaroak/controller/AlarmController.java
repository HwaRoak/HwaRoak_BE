package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.request.AlarmRequestDto;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alarm", description = "알림 관련 API")
@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "알림함 전체 조회", description = "로그인한 사용자의 모든 알람을 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
    @GetMapping
    public List<AlarmResponseDto.InfoDto> getAlarmList() {
        return alarmService.getAllAlarmsForMember();
    }

    @Operation(summary = "공지 목록 조회", description = "alarmType = NOTIFICATION 인 공지를 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 목록 조회 성공")
    @GetMapping("/notices")
    public List<AlarmResponseDto.PreviewDto> getNoticeList() {
        return alarmService.getNoticeList();
    }

    @Operation(summary = "공지 상세 조회", description = "alarmType = NOTIFICATION 인 공지의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 상세 조회 성공")
    @ApiResponse(responseCode = "4041", description = "공지 없음 (NOTICE_NOT_FOUND)")
    @GetMapping("/{noticeId}")
    public AlarmResponseDto.InfoDto getNoticeDetail(@PathVariable Long noticeId) {
        return alarmService.getNoticeDetail(noticeId);
    }

    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    @ApiResponse(responseCode = "4041", description = "알림 없음 (ALARM_NOT_FOUND)")
    @PatchMapping("/{id}/read")
    public void readAlarm(@PathVariable("id") Long alarmId) {
        alarmService.markAsRead(alarmId);
    }

    @Operation(summary = "공지 등록", description = "관리자가 공지를 수동 등록합니다.")
    @ApiResponse(responseCode = "201", description = "공지 등록 성공")
    @PostMapping("/notices")
    public void createNotice(@RequestBody AlarmRequestDto.CreateNoticeDto requestDto) {
        alarmService.createNotice(requestDto);
    }


}
