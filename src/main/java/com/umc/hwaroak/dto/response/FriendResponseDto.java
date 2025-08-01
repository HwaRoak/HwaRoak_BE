package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class FriendResponseDto {

    /**
     * 친구 목록 조회 시 사용하는 친구 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class FriendInfo {

        @Schema(description = "친구의 UserId", example = "#@#12")
        private String userId;

        @Schema(description = "친구 닉네임", example = "햇살가득이")
        private String nickname;

        @Schema(description = "친구 한줄소개", example = "오늘도 잘 부탁해요~")
        private String introduction;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReceivedRequestInfo {

        @Schema(description = "받은 친구 요청의 UserId", example = "#@#12")
        private String userId;

        @Schema(description = "요청 보낸 사용자 닉네임", example = "감자소년")
        private String nickname;

        @Schema(description = "요청 보낸 사용자의 자기소개", example = "함께 친구해요 :)")
        private String introduction;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SearchResultDto {

        @Schema(description = "찾은 유저의 UserId", example = "#!2231!")
        private String userId;     // 유저 ID (공개용)
        @Schema(description = "찾은 유저의 NickName", example = "나는야 화록이")
        private String nickname;
        @Schema(description = "찾은 유저의 자기소개", example = "안녕하세요~")
        private String introduction;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FriendPageInfo {
        @Schema(description = "친구의 userId", example = "#@!3132")
        private String userId;
        @Schema(description = "친구의 닉네임", example = "화록이2")
        private String nickname;
        @Schema(description = "친구의 그날 감정 분석", example = "화록이2님은 깔끼해요 or 불씨를 지펴보세요!")
        private String message;  // GPT 응답 or 디폴트 메시지
    }

}
