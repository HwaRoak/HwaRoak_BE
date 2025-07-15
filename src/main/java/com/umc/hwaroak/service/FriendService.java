package com.umc.hwaroak.service;

public interface FriendService {
    void requestFriend(Long receiverId);

    void acceptFriendRequest(Long senderId);
}
