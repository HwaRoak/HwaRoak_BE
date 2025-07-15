package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.FriendRequestDto;
import com.umc.hwaroak.dto.FriendResponseDto;
import com.umc.hwaroak.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend", description = "친구 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기", description = "상대방에게 친구 요청을 보냅니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 성공")
    @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자 (MEMBER_NOT_FOUND) 또는 자기 자신에게 요청 (CANNOT_ADD_SELF) 또는 중복 요청 (FRIEND_ALREADY_EXISTS_OR_REQUESTED)")
    @PostMapping("/request")
    public void requestFriend(@RequestBody FriendRequestDto.Request requestDto) {
        friendService.requestFriend(requestDto.getReceiverId());
    }

    @Operation(summary = "친구 요청 수락", description = "상대방이 보낸 친구 요청을 수락합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 수락 성공")
    @ApiResponse(responseCode = "400", description = "이미 처리된 요청 (FRIEND_REQUEST_NOT_PENDING)")
    @ApiResponse(responseCode = "404", description = "요청 보낸 사용자 없음 (MEMBER_NOT_FOUND) 또는 친구 요청 없음 (FRIEND_REQUEST_NOT_FOUND)")
    @PostMapping("/accept")
    public void acceptFriendRequest(@RequestBody FriendRequestDto.Accept requestDto) {
        friendService.acceptFriendRequest(requestDto.getSenderId());
    }

    @Operation(summary = "친구 요청 거절", description = "상대방이 보낸 친구 요청을 거절합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 거절 성공")
    @ApiResponse(responseCode = "400", description = "이미 처리된 요청 (FRIEND_REQUEST_NOT_PENDING)")
    @ApiResponse(responseCode = "404", description = "요청 보낸 사용자 없음 (MEMBER_NOT_FOUND) 또는 친구 요청 없음 (FRIEND_REQUEST_NOT_FOUND)")
    @PostMapping("/reject")
    public void rejectFriendRequest(@RequestBody FriendRequestDto.Reject requestDto) {
        friendService.rejectFriendRequest(requestDto.getSenderId());
    }

    @Operation(summary = "친구 목록 조회", description = "현재 로그인한 유저의 친구 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "친구 목록 조회 성공")
    @GetMapping
    public List<FriendResponseDto.FriendInfo> getFriendList() {
        return friendService.getFriendList();
    }
}
