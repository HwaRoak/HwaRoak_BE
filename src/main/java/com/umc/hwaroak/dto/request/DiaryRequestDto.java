package com.umc.hwaroak.dto.request;

import com.umc.hwaroak.domain.common.Emotion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "일기 기록 요청 DTO")
public class DiaryRequestDto {

    @Schema(name = "기록 날짜",
    example = "2025-07-13")
    private LocalDate recordDate;
    @Schema(name = "기록 내용")
    private String content;
    @Schema(name = "감정",
            allowableValues = {"CALM", "PROUD", "HAPPY", "EXPECTED", "HEART_FLUTTER",
            "THANKFUL", "EXCITING", "EXCITING", "SADNESS", "ANGRY", "BORED", "TIRED",
            "ANNOYED", "LONELY", "GLOOMY", "STRESSFUL"})
    private Emotion emotion;
}
