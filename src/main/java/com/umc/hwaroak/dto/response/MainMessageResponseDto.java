package com.umc.hwaroak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MainMessageResponseDto {
    private String message;

    public static MainMessageResponseDto of(String message) {
        return MainMessageResponseDto.builder()
                .message(message)
                .build();
    }
}