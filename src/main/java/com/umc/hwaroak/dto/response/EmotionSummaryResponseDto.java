package com.umc.hwaroak.dto.response;

import com.umc.hwaroak.domain.common.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

public class EmotionSummaryResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "이번달 감정분석 간단 조회 응답 DTO")
    public static class PreviewDto{
        private Map<EmotionCategory, EmotionCount> emotionSummary;
    }

    @Getter
    @AllArgsConstructor
    public static class EmotionCount {
        private int number;
        private double percent;
    }


}
