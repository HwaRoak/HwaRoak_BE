package com.umc.hwaroak.dto.response;

import com.umc.hwaroak.domain.common.Item;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "일기 작성 응답 DTO")
public class DiaryResponseDto {

    @Schema(description = "일기 ID")
    private Long id;
    @Schema(description = "기록한 감정")
    private List<String> emotionList;
    @Schema(description = "감정 피드백",
    example = "오늘은 참 재미있는 일이 있었네!>ㅁ<")
    private String feedback;
    @Schema(description = "리워드")
    private Integer reward;
    @Schema(description = "다음 아이템")
    private Item item;

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailDto {

        @Schema(description = "일기 ID")
        private Long id;
        @Schema(description = "일기 작성 날짜")
        private LocalDate recordDate;
        @Schema(description = "기록한 감정")
        private List<String> emotionList;
        @Schema(description = "일기 본문")
        private String content;
    }
}
