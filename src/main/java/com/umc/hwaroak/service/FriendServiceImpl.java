package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.FriendStatus;
import com.umc.hwaroak.dto.FriendResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.FriendRepository;
import com.umc.hwaroak.repository.MemberRepository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;


    /**
     * 현재 로그인된 사용자를 임시로 반환합니다.
     * TODO: 이후 JWT 인증 정보를 기반으로 실제 로그인 유저를 반환하도록 수정 필요
     */
    private Member getCurrentMember() {
        return memberRepository.findById(1L) // 임시 고정 ID
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
    }

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




    @Override
    @Transactional
    public void acceptFriendRequest(Long senderId) {
        // [1] 현재 로그인한 유저 (친구 요청을 받은 사람 = receiver)
        Member receiver = getCurrentMember();

        // [2] 요청 보낸 sender 유저가 존재하는지 확인
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // [3] sender → receiver로 상태가 REQUESTED인 친구 요청 찾기
        Friend friendRequest = friendRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        // [4] 이미 수락되었거나 거절된 요청인 경우 수락 불가
        if (friendRequest.getStatus() != FriendStatus.REQUESTED) {
            throw new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
        }

        // [5] 요청 상태를 ACCEPTED로 변경
        friendRequest.updateStatus(FriendStatus.ACCEPTED);
    }


    @Override
    @Transactional
    public void rejectFriendRequest(Long senderId) {
        // [1] 로그인한 유저 (receiver)
        Member receiver = getCurrentMember();

        // [2] 요청 보낸 유저(sender) 존재 여부 확인
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // [3] sender → receiver 요청 존재 확인
        Friend friendRequest = friendRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        // [4] 상태가 REQUESTED 인지 확인
        if (friendRequest.getStatus() != FriendStatus.REQUESTED) {
            throw new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
        }

        // [5] 상태를 REJECTED 로 변경
        friendRequest.updateStatus(FriendStatus.REJECTED);
    }


    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto.FriendInfo> getFriendList() {
        // [1] 현재 로그인된 사용자 (나)
        Member me = getCurrentMember();

        // [2] 내가 sender 또는 receiver로 포함된 친구 관계 중, 상태가 ACCEPTED인 것들 모두 조회
        // → 단방향으로 저장되어 있기 때문에 sender 또는 receiver 둘 다 체크 필요
        List<Friend> acceptedFriends = friendRepository.findAllBySenderOrReceiverAndStatus(me, me, FriendStatus.ACCEPTED);

        // [3] 각 친구 관계에서 "나"와 반대쪽에 있는 Member만 추출
        // → 그게 진짜 '친구'임
        return acceptedFriends.stream()
                .map(friend -> {
                    Member friendMember = friend.getSender().equals(me)
                            ? friend.getReceiver()  // 내가 sender일 경우 → 친구는 receiver
                            : friend.getSender();  // 내가 receiver일 경우 → 친구는 sender

                    // [4] 친구 정보를 응답 DTO로 변환
                    return FriendResponseDto.FriendInfo.builder()
                            .memberId(friendMember.getId())
                            .nickname(friendMember.getNickname())
                            .introduction(friendMember.getIntroduction())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto.ReceivedRequestInfo> getReceivedFriendRequests() {
        Member me = getCurrentMember();

        // [1] 상태가 REQUESTED이고, 내가 받은 요청만 최신순 정렬로 조회
        List<Friend> requests = friendRepository.findAllByReceiverAndStatusOrderByCreatedAtDesc(
                me, FriendStatus.REQUESTED
        );

        // [2] 요청 보낸 사람 정보를 DTO로 변환
        return requests.stream()
                .map(friend -> {
                    Member sender = friend.getSender();
                    return FriendResponseDto.ReceivedRequestInfo.builder()
                            .memberId(sender.getId())
                            .nickname(sender.getNickname())
                            .introduction(sender.getIntroduction())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
