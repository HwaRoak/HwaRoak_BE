package com.umc.hwaroak.dto.response;

import com.umc.hwaroak.domain.common.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

public class MemberResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 정보 조회 응답 DTO")
    public static class InfoDto {

        @Schema(name = "회원ID", description = "파싱 처리 위한 회원 ID")
        private Long memberId;

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

        @Schema(description = "item_id", example = "1")
        Long item_id;

        @Schema(description = "아이템 이름", example = "두루마리 휴지")
        String name;

        @Schema(description = "아이템 레벨", example = "1")
        Integer level;

        @Schema(description = "선택 여부", example = "false")
        Boolean isSelected;
    }

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
    @Schema(description = "이번달 감정분석 간단 조회 응답 DTO",
            example = """
        {
            "CALM": {"number": 3, "percent": 23.1},
            "HAPPY": { "number": 5, "percent": 38.5},
            "SAD": {"number": 1, "percent": 7.7},
            "ANGRY": {"number": 4, "percent": 30.8}
        }
        """)
    public static class PreviewDto{
        private Map<EmotionCategory, EmotionCount> emotionSummary;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "감정분석 조회 응답 DTO")
    public static class DetailDto{
        private int diaryCount;
        private PreviewDto emotionSummary;
        private String message;
    }
}
