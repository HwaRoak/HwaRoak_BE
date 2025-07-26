package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.response.FireAlarmResponseDto;
import com.umc.hwaroak.dto.response.FriendResponseDto;

import java.util.List;

public interface FriendService {
    void requestFriend(String receiverUserId);

    void acceptFriendRequest(String senderUserId);

    void rejectFriendRequest(String senderUserId);

    List<FriendResponseDto.FriendInfo> getFriendList();

    List<FriendResponseDto.ReceivedRequestInfo> getReceivedFriendRequests();

    void deleteFriend(String friendMemberUserId);

    FriendResponseDto.SearchResultDto searchFriendByUserId(String userId);

    FireAlarmResponseDto fireFriend(String friendUserId);

    boolean isFriend(Member member1, Member member2);
}
