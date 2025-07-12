package com.umc.hwaroak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfoDTO{

        @Schema(description = "유저 아이디", example = "a123456789")
        String userId;

        @Schema(description = "닉네임", example = "테스트 닉네임")
        String nickname;

        // Todo: 프로필 사진 url

        @Schema(description = "자기소개", example = "안녕하세요.")
        String introduction;

    }
}
