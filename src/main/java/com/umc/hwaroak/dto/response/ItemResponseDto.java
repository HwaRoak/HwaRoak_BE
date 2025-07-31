package com.umc.hwaroak.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema
public class ItemResponseDto {

    @Getter
    @Schema
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public class ReceivedDto {
        private Long id;
        private String name;
        private int dDay;
    }

    @Getter
    @Schema
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public class NextDto {
        private String name;
        private int dDay;
    }
}
