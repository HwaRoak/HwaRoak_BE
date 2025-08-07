package com.umc.hwaroak.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class DiaryRequestDto {

    @Schema(description = "일기 기록 요청 DTO")
    @Getter
    @Setter
    public static class CreateDto {
        @Schema(description = "기록 날짜",
                example = "2025-07-13")
        private LocalDate recordDate;
        @Schema(description = "기록 내용")
        private String content;
        @Schema(description = "기록할 감정(최대 3개 작성)",
                example = "[\"짜증남\", \"화나는\", \"스트레스\"]")
        private List<String> emotionList;
    }
}
