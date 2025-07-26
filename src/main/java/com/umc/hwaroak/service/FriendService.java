package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.response.FireAlarmResponseDto;
import com.umc.hwaroak.dto.response.FriendResponseDto;

import java.util.List;

public interface FriendService {
    void requestFriend(String receiverUserId);

    void acceptFriendRequest(Long senderId);

    void rejectFriendRequest(Long senderId);

    List<FriendResponseDto.FriendInfo> getFriendList();

    List<FriendResponseDto.ReceivedRequestInfo> getReceivedFriendRequests();

    void deleteFriend(Long friendMemberId);
    FireAlarmResponseDto fireFriend(Long friendId);

    boolean isFriend(Member member1, Member member2);
}
