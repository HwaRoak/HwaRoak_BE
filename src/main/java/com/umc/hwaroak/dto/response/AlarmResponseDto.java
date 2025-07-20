package com.umc.hwaroak.dto.response;

import com.umc.hwaroak.domain.common.AlarmType;
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

        @Schema(description = "알람 ID", example = "1")
        private Long id;

        @Schema(description = "알람 제목", example = "서버 점검 안내")
        private String title;

        @Schema(description = "알람 생성일", example = "2025-07-14")
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

        @Schema(description = "알람 ID", example = "1")
        private Long id;

        @Schema(description = "알람 제목", example = "친구 요청")
        private String title;

        @Schema(description = "공지 내용", example = "OO님이 친구요청을 보냈습니다.")
        private String content;

        @Schema(description = "알람 종류", example = "친구요청")
        private AlarmType alarmType;

        @Schema(description = "알람 생성일", example = "2025-07-14")
        private LocalDate createdAt;
    }
}
