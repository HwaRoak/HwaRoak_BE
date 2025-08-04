package com.umc.hwaroak.dto.response;

import com.umc.hwaroak.domain.common.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

public class MemberResponseDto {

    /*
    * 회원 정보 조회 응답
    * */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "MemberResponseDto", description = "회원 정보 조회 응답 DTO")
    public static class InfoDto {

        @Schema(description = "파싱 처리 위한 회원 ID")
        private Long memberId;

        @Schema(description = "유저 아이디", example = "a1b2c3d4e5f6g7h8")
        String userId;

        @Schema(description = "닉네임", example = "테스트 닉네임")
        String nickname;

        @Schema(description = "프로필 사진 url", example = "https://example.com/image.png")
        String profileImgUrl;

        @Schema(description = "자기소개", example = "안녕하세요.")
        String introduction;

    }

    /*
    * 마이페이지 렌더링 데이터 조회 응답
    * */
    @Getter
    @AllArgsConstructor
    public static class EmotionCount {
        private int number;
        private double percent;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로필 이미지 업로드 응답 DTO")
    public static class ProfileImageDto {
        @Schema(description = "업로드된 프로필 이미지 URL", example = "https://your-bucket.s3.amazonaws.com/user123/profile/filename.jpg")
        private String profileImageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviewDto{

        @Schema(description = "닉네임")
        private String nickname;

        @Schema(description = "프로필 사진 url", example = "https://example.com/image.png")
        private String profileImgUrl;

        @Schema(description = "이번달 감정분석 간단 조회 응답 DTO",
                example = """
        {
            "CALM": {"number": 3, "percent": 23.1},
            "HAPPY": { "number": 5, "percent": 38.5},
            "SAD": {"number": 1, "percent": 7.7},
            "ANGRY": {"number": 4, "percent": 30.8}
        }
        """)
        private Map<EmotionCategory, EmotionCount> emotionSummary;

        @Schema(description = "누적 일기 개수")
        private Long totalDiary;

        @Schema(description = "리워드까지 남은 일자")
        private Integer reward;

        @Schema(description = "다음 아이템 이름")
        private String nextItemName;


    }

    /*
    * 감정분석 상세 조회 응답
    * */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "감정분석 조회 응답 DTO")
    public static class DetailDto{

        @Schema(description = "해당 달에 작성된 일기 개수")
        private int diaryCount;

        @Schema(description = "감정 통계",
                example = """
        {
            "CALM": {"number": 3, "percent": 23.1},
            "HAPPY": { "number": 5, "percent": 38.5},
            "SAD": {"number": 1, "percent": 7.7},
            "ANGRY": {"number": 4, "percent": 30.8}
        }
        """)
        private Map<EmotionCategory, EmotionCount> emotionSummary;

        @Schema(description = "gpt 활용 감정분석 메시지")
        private String message;
    }
}
