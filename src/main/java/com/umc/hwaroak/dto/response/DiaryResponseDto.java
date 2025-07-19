package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "일기 작성 응답 DTO")
public class DiaryResponseDto {

    @Schema(description = "일기 ID")
    private Long id;
    @Schema(description = "감정 피드백",
    example = "오늘은 참 재미있는 일이 있었네!>ㅁ<")
    private String feedback;
    @Schema(description = "리워드")
    private Integer reward;
    @Schema(description = "다음 아이템")
    private Long memberItemId;
}
