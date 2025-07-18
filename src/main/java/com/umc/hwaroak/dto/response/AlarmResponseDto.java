package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class AlarmResponseDto {

    /**
     * 최신순 정렬용 Dto
     */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewDto {

        @Schema(description = "공지 ID", example = "1")
        private Long id;

        @Schema(description = "공지 제목", example = "서버 점검 안내")
        private String title;

        @Schema(description = "공지 생성일", example = "2025-07-14")
        private LocalDate createdAt; // 프론트에서 언제 올라온 공지인지 정렬 및 표시용
    }

    /**
     * 공지 상세보기용 Dto
     */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfoDto {

        @Schema(description = "공지 ID", example = "1")
        private Long id;

        @Schema(description = "공지 제목", example = "서버 점검 안내")
        private String title;

        @Schema(description = "공지 내용", example = "7월 16일 03시부터 서버 점검이 진행됩니다.")
        private String content;

        @Schema(description = "공지 생성일", example = "2025-07-14")
        private LocalDate createdAt;
    }
}
