package com.umc.hwaroak.controller;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.FriendRequestDto;
import com.umc.hwaroak.dto.response.FireAlarmResponseDto;
import com.umc.hwaroak.dto.response.FriendResponseDto;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.response.SuccessCode;
import com.umc.hwaroak.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Friend", description = "친구 기능 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청 보내기", description = "상대방에게 친구 요청을 보냅니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 성공")
    @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자 (MEMBER_NOT_FOUND) 또는 자기 자신에게 요청 (CANNOT_ADD_SELF) 또는 중복 요청 (FRIEND_ALREADY_EXISTS_OR_REQUESTED)")
    @PostMapping("/request")
    public void requestFriend(@RequestBody FriendRequestDto.Request requestDto) {
        friendService.requestFriend(requestDto.getReceiverUserId());
    }

    @Operation(summary = "친구 요청 수락", description = "상대방이 보낸 친구 요청을 수락합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 수락 성공")
    @ApiResponse(responseCode = "400", description = "이미 처리된 요청 (FRIEND_REQUEST_NOT_PENDING)")
    @ApiResponse(responseCode = "404", description = "요청 보낸 사용자 없음 (MEMBER_NOT_FOUND) 또는 친구 요청 없음 (FRIEND_REQUEST_NOT_FOUND)")
    @PostMapping("/{friendId}/accept")
    public void acceptFriendRequest(@PathVariable Long friendId) {
        friendService.acceptFriendRequest(friendId);
    }

    @Operation(summary = "친구 요청 거절", description = "상대방이 보낸 친구 요청을 거절합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 거절 성공")
    @ApiResponse(responseCode = "400", description = "이미 처리된 요청 (FRIEND_REQUEST_NOT_PENDING)")
    @ApiResponse(responseCode = "404", description = "요청 보낸 사용자 없음 (MEMBER_NOT_FOUND) 또는 친구 요청 없음 (FRIEND_REQUEST_NOT_FOUND)")
    @PostMapping("/{friendId}/reject")
    public void rejectFriendRequest(@PathVariable Long friendId) {
        friendService.rejectFriendRequest(friendId);
    }

    @Operation(summary = "친구 목록 조회", description = "현재 로그인한 유저의 친구 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "친구 목록 조회 성공")
    @GetMapping
    public List<FriendResponseDto.FriendInfo> getFriendList() {
        return friendService.getFriendList();
    }

    @Operation(summary = "받은 친구 요청 목록 조회", description = "아직 수락/거절되지 않은, 내가 받은 친구 요청 목록을 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "받은 친구 요청 목록 조회 성공")
    @GetMapping("/received")
    public List<FriendResponseDto.ReceivedRequestInfo> getReceivedFriendRequests() {
        return friendService.getReceivedFriendRequests();
    }

    @Operation(summary = "친구 삭제", description = "현재 친구인 사용자를 친구 목록에서 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "친구 삭제 성공")
    @ApiResponse(responseCode = "404", description = "친구 관계가 존재하지 않음 (FRIEND_NOT_FOUND)")
    @DeleteMapping("/{friendId}")
    public void deleteFriend(@PathVariable Long friendId) {
        friendService.deleteFriend(friendId);
    }

    @Operation(summary = "친구에게 불씨 보내기", description = "친구에게 ‘불 키우기’ 알림을 전송합니다.")
    @ApiResponse(responseCode = "200", description = "불 지피기 성공")
    @ApiResponse(responseCode = "403", description = "해당 사용자와 친구 관계가 아닙니다")
    @PostMapping("/{friendId}/fire")
    public FireAlarmResponseDto fireFriend(@PathVariable Long friendId) {
        return friendService.fireFriend(friendId);
    }
}
