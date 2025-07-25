package com.umc.hwaroak.dto.response;

import com.umc.hwaroak.domain.common.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

public class EmotionSummaryResponseDto {

    @Getter
    @AllArgsConstructor
    public static class EmotionCount {
        private int number;
        private double percent;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "이번달 감정분석 간단 조회 응답 DTO",
            example = """
        {
            "CALM": {"number": 3, "percent": 23.1},
            "HAPPY": { "number": 5, "percent": 38.5},
            "SAD": {"number": 1, "percent": 7.7},
            "ANGRY": {"number": 4, "percent": 30.8}
        }
        """)
    public static class PreviewDto{
        private Map<EmotionCategory, EmotionCount> emotionSummary;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "감정분석 조회 응답 DTO")
    public static class DetailDto{
        private int diaryCount;
        private PreviewDto emotionSummary;
        private String message;
    }

}
