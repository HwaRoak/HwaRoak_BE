package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class FriendResponseDto {

    /**
     * 친구 목록 조회 시 사용하는 친구 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class FriendInfo {
        @Schema(description = "친구의 userId", example = "friend_123")
        private String userId;

        @Schema(description = "친구 닉네임", example = "화록이2")
        private String nickname;

        @Schema(description = "친구 소개글", example = "안녕하세요! 화록 좋아요")
        private String introduction;

        @Schema(description = "프로필 이미지 URL (없으면 null 또는 기본이미지 사용)", example = "https://.../profile.jpg")
        private String profileImage;
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

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FriendItemsInfo {
        @Schema(description = "친구의 아이템 PK 리스트", example = "[1, 2, 3, 5]")
        private List<Long> items;

        @Schema(description = "친구가 선택한 아이템의 PK", example = "3")
        private Long selectedItem;
    }

}
