package com.umc.hwaroak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FriendRequestDto {

    @Getter
    @NoArgsConstructor
    public static class Request {

        @Schema(description = "요청받을 친구의 member ID", example = "2")
        private Long receiverId;
    }
}
