package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 정보 조회 응답 DTO")
    public static class InfoDto {

        @Schema(description = "유저 아이디", example = "a123456789")
        String userId;

        @Schema(description = "닉네임", example = "테스트 닉네임")
        String nickname;

        @Schema(description = "프로필 사진 url", example = "https://example.com/image.png")
        String profileImgUrl;

        @Schema(description = "자기소개", example = "안녕하세요.")
        String introduction;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "보유 아이템 조회 응답 DTO")
    public static class ItemDto {

        @Schema(description = "id", example = "1")
        Long memberItemId;

        @Schema(description = "아이템 이름", example = "두루마리 휴지")
        String name;

        @Schema(description = "아이템 레벨", example = "1")
        Integer level;

        @Schema(description = "선택 여부", example = "false")
        Boolean isSelected;
    }
}
