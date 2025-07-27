package com.umc.hwaroak.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.umc.hwaroak.domain.common.AlarmType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(name = "알람 응답 DTO")
public class AlarmResponseDto {

    /**
     * 알람 제목만.
     */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PreviewDto {

        @Schema(description = "알람 ID", example = "1")
        private Long id;

        @Schema(description = "수신자 ID", example = "1")
        private Long receiverId;

        @Schema(description = "알람 제목", example = "서버 점검 안내")
        private String title;

        @Schema(description = "알람 타입")
        private AlarmType alarmType;

        @Schema(description = "알람 메세지", example = "서버 점검 공지를 확인해보세요.")
        private String message;

        @Schema(description = "알람 생성일")
        private LocalDateTime createdAt; // 프론트에서 언제 올라온 공지인지 정렬 및 표시용
    }

    /**
     * 알람 제목 + content(내용) , 현재 message 안쓰는중
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

        @Schema(description = "알람 종류", example = "FRIEND_REQUEST")
        private AlarmType alarmType;

        @Schema(description = "알람 생성일")
        private LocalDateTime createdAt;
    }
}
