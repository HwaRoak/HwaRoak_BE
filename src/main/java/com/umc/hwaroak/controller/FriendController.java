package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.FireAlarmResponseDto;
import com.umc.hwaroak.dto.response.FriendResponseDto;
import com.umc.hwaroak.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/request/{userId}")
    public void requestFriend(@PathVariable String userId) {
        friendService.requestFriend(userId);
    }

    @Operation(summary = "친구 요청 수락", description = "상대방이 보낸 친구 요청을 수락합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 수락 성공")
    @ApiResponse(responseCode = "400", description = "이미 처리된 요청 (FRIEND_REQUEST_NOT_PENDING)")
    @ApiResponse(responseCode = "404", description = "요청 보낸 사용자 없음 (MEMBER_NOT_FOUND) 또는 친구 요청 없음 (FRIEND_REQUEST_NOT_FOUND)")
    @PostMapping("/{userId}/accept")
    public void acceptFriendRequest(@PathVariable String userId) {
        friendService.acceptFriendRequest(userId);
    }

    @Operation(summary = "친구 요청 거절", description = "상대방이 보낸 친구 요청을 거절합니다.")
    @ApiResponse(responseCode = "200", description = "친구 요청 거절 성공")
    @ApiResponse(responseCode = "400", description = "이미 처리된 요청 (FRIEND_REQUEST_NOT_PENDING)")
    @ApiResponse(responseCode = "404", description = "요청 보낸 사용자 없음 (MEMBER_NOT_FOUND) 또는 친구 요청 없음 (FRIEND_REQUEST_NOT_FOUND)")
    @PostMapping("/{userId}/reject")
    public void rejectFriendRequest(@PathVariable String userId) {
        friendService.rejectFriendRequest(userId);
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
    @DeleteMapping("/{userId}")
    public void deleteFriend(@PathVariable String userId) {
        friendService.deleteFriend(userId);
    }

    @GetMapping("/search/{userId}")
    @Operation(summary = "userId로 회원 검색", description = "입력한 userId를 가진 회원을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공", content = @Content(schema = @Schema(implementation = FriendResponseDto.SearchResultDto.class)))
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음 (MEMBER_NOT_FOUND)")
    public FriendResponseDto.SearchResultDto searchFriend(@PathVariable String userId) {
        return friendService.searchFriendByUserId(userId);
    }


    @Operation(summary = "친구에게 불씨 보내기", description = "친구에게 ‘불 키우기’ 알림을 전송합니다.")
    @ApiResponse(responseCode = "200", description = "불 지피기 성공")
    @ApiResponse(responseCode = "403", description = "해당 사용자와 친구 관계가 아닙니다")
    @PostMapping("/{userId}/fire")
    public FireAlarmResponseDto fireFriend(@PathVariable String userId) {
        return friendService.fireFriend(userId);
    }

    @Operation(summary = "친구 페이지 방문하기", description = "친구 페이지를 방문합니다.")
    @ApiResponse(responseCode = "200", description = "친구 정보 조회 성공")
    @ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다")
    @GetMapping("/{userId}")
    public FriendResponseDto.FriendPageInfo getFriendPage(@PathVariable String userId) {
        return friendService.getFriendPage(userId);
    }
}
