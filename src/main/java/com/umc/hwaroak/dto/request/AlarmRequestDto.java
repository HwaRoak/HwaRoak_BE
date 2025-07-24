package com.umc.hwaroak.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class AlarmRequestDto {

    @Getter
    @NoArgsConstructor
    public static class CreateNoticeDto {
        private String title;
        private String content;
        private String message;
    }
}
