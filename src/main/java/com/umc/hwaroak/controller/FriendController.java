package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.FriendRequestDto;
import com.umc.hwaroak.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Friend", description = "친구 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기", description = "상대방에게 친구 요청을 보냅니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 성공")
    @PostMapping("/request")
    public void requestFriend(@RequestBody FriendRequestDto.Request requestDto) {
        friendService.requestFriend(requestDto.getReceiverId());
    }
}
