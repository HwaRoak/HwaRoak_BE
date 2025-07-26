package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "일기 DTO")
public class DiaryResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "일기 작성 응답 DTO")
    public static class CreateDto {
        @Schema(description = "일기 ID")
        private Long id;
        @Schema(description = "일기 작성 날짜")
        private LocalDateTime recordDate;
        @Schema(description = "기록한 감정")
        private List<String> emotionList;
        @Schema(description = "감정 피드백",
                example = "오늘은 참 재미있는 일이 있었네!>ㅁ<")
        private String feedback;
        @Schema(description = "리워드")
        private Integer reward;
        @Schema(description = "다음 아이템 이름")
        private String memberItemName;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "일기 요약 관련 DTO")
    public static class ThumbnailDto {
        @Schema(description = "일기 ID")
        private Long id;
        @Schema(description = "일기 작성 날짜")
        private LocalDateTime recordDate;
        @Schema(description = "기록한 감정")
        private List<String> emotionList;
        @Schema(description = "감정 피드백",
                example = "오늘은 참 재미있는 일이 있었네!>ㅁ<")
        private String feedback;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "일기 상세보기 응답 DTO")
    public static class DetailDto {

        @Schema(description = "일기 ID")
        private Long id;
        @Schema(description = "일기 작성 날짜")
        private LocalDateTime recordDate;
        @Schema(description = "기록한 감정")
        private List<String> emotionList;
        @Schema(description = "일기 본문")
        private String content;
    }
}
