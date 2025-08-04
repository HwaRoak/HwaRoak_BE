package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema
public class ItemResponseDto {

    /*
     * 보유 아이템 조회 응답
     * */
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "보유 아이템 조회 응답 DTO")
    public static class ItemDto {

        @Schema(description = "item_id", example = "1")
        Long itemId;

        @Schema(description = "아이템 이름", example = "두루마리 휴지")
        String name;

        @Schema(description = "아이템 레벨", example = "1")
        Integer level;

        @Schema(description = "선택 여부", example = "false")
        Boolean isSelected;

        @Schema(description = "보상 받기 완료 여부", example = "false")
        Boolean isReceived;
    }

    @Getter
    @Schema(description = "아직 받지 못한 다음 아이템 이름과 남은 기한 응답 DTO")
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class NextDto {
        private String name;
        private int dDay;
    }
}
