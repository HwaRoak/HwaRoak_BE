package com.umc.hwaroak.dto;

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

        @Schema(description = "친구의 member ID", example = "5")
        private Long memberId;

        @Schema(description = "친구 닉네임", example = "햇살가득이")
        private String nickname;

        @Schema(description = "친구 한줄소개", example = "오늘도 잘 부탁해요~")
        private String introduction;
    }
}
