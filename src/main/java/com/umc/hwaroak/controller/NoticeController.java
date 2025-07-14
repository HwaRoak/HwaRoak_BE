package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.NoticeResponseDto;
import com.umc.hwaroak.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notice", description = "공지 관련 API")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final AlarmService alarmService;

    @Operation(summary = "공지 목록 조회", description = "최신순으로 공지 제목 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 목록 조회 성공")
    @GetMapping
    public List<NoticeResponseDto.PreviewDto> getNoticeList() {
        return alarmService.getNoticeList();
    }

    @Operation(summary = "공지 상세 조회", description = "공지 ID를 통해 상세 내용을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공지 상세 조회 성공")
    @ApiResponse(responseCode = "4041", description = "공지 없음 (NOTICE_NOT_FOUND)")
    @GetMapping("/{id}")
    public NoticeResponseDto.InfoDto getNoticeDetail(@PathVariable Long id) {
        return alarmService.getNoticeDetail(id);
    }
}
