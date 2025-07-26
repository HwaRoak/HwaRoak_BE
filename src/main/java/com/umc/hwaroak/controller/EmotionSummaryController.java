package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.EmotionSummaryResponseDto;
import com.umc.hwaroak.service.EmotionSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@Tag(name = "EmotionSummary API", description = "감정분석 관련 API")
@RestController
@RequestMapping("/api/v1/members/emotion")
@RequiredArgsConstructor
public class EmotionSummaryController {

    private final EmotionSummaryService emotionSummaryService;

    @GetMapping("/preview")
    @Operation(summary = "이번달 감정분석 preview 조회",
            description = "이번달 감정 카테고리별 개수와 비율을 조회합니다. 반올림 때문에 비율 총합이 100이 넘을 수도 있습니다. 분석할 데이터가 없는 경우 null이 반환됩니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = EmotionSummaryResponseDto.PreviewDto.class)))
    public EmotionSummaryResponseDto.PreviewDto getEmotionSummary() {

        // 오늘 날짜 기준으로 이번 달 구하기
        String yearMonth = YearMonth.now().toString(); // ex. "2025-07"

        return emotionSummaryService.getPreviewEmotionSummary(yearMonth);
    }

    @GetMapping("/{summaryMonth}")
    @Operation(summary = "감정분석 상세 조회", description = "특정달 감정분석을 조회합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = EmotionSummaryResponseDto.DetailDto.class)))
    public EmotionSummaryResponseDto.DetailDto getDetailEmotionSummary(
            @Schema(description = "조회할 연월", example = "2025-07")
            @PathVariable String summaryMonth
    ) {
        return emotionSummaryService.getDetailEmotionSummary(summaryMonth);
    }
}
