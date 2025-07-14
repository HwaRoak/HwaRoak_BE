package com.umc.hwaroak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NoticeResponseDto {
    /**
     * 최신순 정렬용 Dto
     */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewDto {
        private Long id;
        private String title;
        private LocalDateTime createdAt; // 프론트에서 언제 올라온 공지인지 정렬 및 표시용
    }

    /**
     *  공지 상세보기용 Dto
     */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfoDto {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;
    }
}
