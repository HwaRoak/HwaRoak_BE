package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.FriendResponseDto;

import java.util.List;

public interface FriendService {
    void requestFriend(Long receiverId);

    void acceptFriendRequest(Long senderId);

    void rejectFriendRequest(Long senderId);

    List<FriendResponseDto.FriendInfo> getFriendList();

    public List<FriendResponseDto.ReceivedRequestInfo> getReceivedFriendRequests();
}
