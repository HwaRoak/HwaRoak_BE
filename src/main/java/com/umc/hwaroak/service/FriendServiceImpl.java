package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.FriendStatus;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.FriendRepository;
import com.umc.hwaroak.repository.MemberRepository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    /**
     * 친구 요청을 보냅니다.
     * 요청 전에 다음 조건을 확인합니다:
     * - 존재하지 않는 유저에게 요청 ❌
     * - 자기 자신에게 요청 ❌
     * - 이미 요청했거나 친구 상태인 경우 ❌
     * 조건을 만족하면 Friend 엔티티를 저장합니다.
     *
     * @param receiverId 친구 요청을 받을 Member의 ID
     */
    @Override
    @Transactional
    public void requestFriend(Long receiverId) {
        // [1] 현재 로그인된 유저를 가져옵니다. (나중에 인증 시스템으로 교체 필요)
        Member sender = getCurrentMember();

        // [2] 요청받을 유저가 존재하는지 확인
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // [3] 자기 자신에게 친구 요청하는 경우 예외 처리
        if (sender.getId().equals(receiver.getId())) {
            throw new GeneralException(ErrorCode.CANNOT_ADD_SELF);
        }

        // [4] 이미 친구 요청을 보냈거나 친구 상태인지 확인 (양방향 모두 검사)
        boolean alreadyExists =
                friendRepository.existsBySenderAndReceiver(sender, receiver) ||
                        friendRepository.existsBySenderAndReceiver(receiver, sender);

        if (alreadyExists) {
            throw new GeneralException(ErrorCode.FRIEND_ALREADY_EXISTS_OR_REQUESTED);
        }

        // [5] 친구 요청 엔티티 생성 및 저장
        Friend friend = new Friend(sender, receiver, FriendStatus.REQUESTED);
        friendRepository.save(friend);
    }

    /**
     * 현재 로그인된 사용자를 임시로 반환합니다.
     * TODO: 이후 JWT 인증 정보를 기반으로 실제 로그인 유저를 반환하도록 수정 필요
     */
    private Member getCurrentMember() {
        return memberRepository.findById(1L) // 임시 고정 ID
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
