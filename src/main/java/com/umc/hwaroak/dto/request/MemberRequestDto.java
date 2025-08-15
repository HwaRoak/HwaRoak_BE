package com.umc.hwaroak.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberRequestDto {

    @Getter
    @Schema(description = "회원 정보 수정 DTO")
    public static class editDto{

        @Schema(description = "수정할 닉네임", nullable = true)
        String nickname;

        @Schema(description = "수정할 소개", nullable = true)
        String introduction;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "프로필 이미지 업로드용 Presigned URL 요청 DTO")
    public static class PresignedUrlRequestDto {

        @Schema(description = "업로드할 파일 Content-Type", example = "image/jpeg", required = true)
        private String contentType;

        @Schema(description = "원본 파일명(확장자 판별/로그용)", example = "profile.jpg", nullable = true)
        private String fileName;

    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "프로필 이미지 업로드 확정 요청 DTO")
    public static class ProfileImageConfirmRequestDto {

        @Schema(description = "S3 오브젝트 키", example = "profiles/1/550e8400-e29b-41d4-a716-446655440000.jpg", required = true)
        private String objectKey;
    }
}
