package com.umc.hwaroak.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "일기 기록 요청 DTO")
public class DiaryRequestDto {

    @Schema(description = "기록 날짜",
    example = "2025-07-13")
    private LocalDateTime recordDate;
    @Schema(description = "기록 내용")
    private String content;
    @Schema(description = "기록할 감정(최대 3개 작성)",
    example = "[\"짜증남\", \"화나는\", \"스트레스\"]")
    private List<String> emotionList;
}
