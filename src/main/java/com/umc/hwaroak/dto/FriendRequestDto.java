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

    @Getter
    @NoArgsConstructor
    public static class Accept {
        @Schema(description = "요청을 보낸 친구의 member ID", example = "1")
        private Long senderId;
    }

    @Getter
    @NoArgsConstructor
    public static class Reject {
        @Schema(description = "친구 요청을 보낸 사람의 member ID", example = "1")
        private Long senderId;
    }

    @Getter
    @NoArgsConstructor
    public static class Delete {
        @Schema(description = "삭제할 친구의 member ID", example = "3")
        private Long memberId;
    }
}
