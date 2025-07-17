package com.umc.hwaroak.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class MemberRequestDto {

    @Getter
    @Schema(description = "회원 정보 수정 DTO")
    public static class editDto{

        @Schema(description = "수정할 닉네임", nullable = true)
        String nickname;

        @Schema(description = "수정할 프로필 사진 url", nullable = true)
        String profileImageUrl;

        @Schema(description = "수정할 소개", nullable = true)
        String introduction;
    }
}
