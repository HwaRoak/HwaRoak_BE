package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.FriendStatus;
import com.umc.hwaroak.dto.response.FireAlarmResponseDto;
import com.umc.hwaroak.dto.response.FriendResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.repository.FriendRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmService;
import com.umc.hwaroak.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalTime.now;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final MemberLoader memberLoader;
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final AlarmService alarmService;

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
        Member sender = memberLoader.getMemberByContextHolder();

        // [2] 요청받을 유저가 존재하는지 확인
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // [3] 자기 자신에게 친구 요청하는 경우 예외 처리
        if (sender.getId().equals(receiver.getId())) {
            throw new GeneralException(ErrorCode.CANNOT_ADD_SELF);
        }

        // [4] 기존 친구 요청/관계가 존재하는지 조회 (단방향 sender → receiver 기준)
        Optional<Friend> existingFriend = friendRepository.findBySenderAndReceiver(sender, receiver);

        if (existingFriend.isPresent()) {
            Friend friend = existingFriend.get();

            // [4-1] 기존 상태가 BLOCKED 또는 REJECTED 이면 상태를 REQUESTED로 바꿔 재요청 처리
            if (friend.getStatus() == FriendStatus.BLOCKED || friend.getStatus() == FriendStatus.REJECTED) {
                friend.updateStatus(FriendStatus.REQUESTED);
                return;
            }

            // [4-2] 그 외 상태라면 중복 요청 예외 처리
            throw new GeneralException(ErrorCode.FRIEND_ALREADY_EXISTS_OR_REQUESTED);
        }

        // [5] 기존 단방향 요청이 없을 경우, 역방향(receiver → sender) 중복 여부도 검사
        boolean reverseExists = friendRepository.existsBySenderAndReceiver(receiver, sender);
        if (reverseExists) {
            throw new GeneralException(ErrorCode.FRIEND_ALREADY_EXISTS_OR_REQUESTED);
        }

        // [6] 친구 요청 엔티티 생성 및 저장
        Friend friend = new Friend(sender, receiver, FriendStatus.REQUESTED);
        friendRepository.save(friend);

        // 👉 알람 전송
        alarmService.sendFriendRequestAlarm(sender, receiver);
    }



    @Override
    @Transactional
    public void acceptFriendRequest(Long senderId) {
        // [1] 현재 로그인한 유저 (친구 요청을 받은 사람 = receiver)
        Member receiver = memberLoader.getMemberByContextHolder();

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
        Member receiver = memberLoader.getMemberByContextHolder();

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
        Member me = memberLoader.getMemberByContextHolder();

        // [2] 내가 sender 또는 receiver로 포함된 친구 관계 중, 상태가 ACCEPTED인 것들 모두 조회
        // → 단방향으로 저장되어 있기 때문에 sender 또는 receiver 둘 다 체크 필요
        List<Friend> acceptedFriends = friendRepository.findAllAcceptedFriends(me);

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
        Member me = memberLoader.getMemberByContextHolder();

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

    @Override
    @Transactional
    public void deleteFriend(Long friendMemberId) {
        Member me = memberLoader.getMemberByContextHolder();
        Member friend = memberRepository.findById(friendMemberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // [1] 나 ↔ 친구 관계에서 상태가 ACCEPTED 인 친구 관계 조회
        Friend relationship = friendRepository.findBySenderAndReceiverAndStatus(me, friend, FriendStatus.ACCEPTED)
                .or(() -> friendRepository.findBySenderAndReceiverAndStatus(friend, me, FriendStatus.ACCEPTED))
                .orElseThrow(() -> new GeneralException(ErrorCode.FRIEND_NOT_FOUND));

        // [2] 관계 상태가 ACCEPTED가 아닌 경우 삭제 불가 (예외 상황 대비용, 안전하게)
        if (relationship.getStatus() != FriendStatus.ACCEPTED) {
            throw new GeneralException(ErrorCode.FRIEND_CANNOT_BE_DELETED);
        }

        // [3] BLOCKED 상태로 변경 (soft delete)
        relationship.updateStatus(FriendStatus.BLOCKED);
    }

    @Override
    @Transactional
    public FireAlarmResponseDto fireFriend(Long friendId) {
        Member sender = memberLoader.getMemberByContextHolder();
        Member receiver = memberRepository.findById(friendId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // 친구 관계인지 확인 (직접 구현하거나 FriendService 내 메서드 활용)
        if (!isFriend(sender, receiver)) {
            throw new GeneralException(ErrorCode.NOT_FRIEND);
        }

        Optional<LocalDateTime> lastFireTimeOpt = alarmService.getLastFireTime(sender, receiver);
        LocalDateTime now = LocalDateTime.now();

        // 60분 쿨타임
        if (lastFireTimeOpt.isPresent()) {
            LocalDateTime lastFireTime = lastFireTimeOpt.get();
            long minutesPassed = Duration.between(lastFireTime, now).toMinutes();

            if (minutesPassed < 60) {
                long minutesLeft = 60 - minutesPassed;
                return FireAlarmResponseDto.builder()
                        .notifiedAt(null)
                        .message("다음 알림은 " + minutesLeft + "분 후에 전송돼요!")
                        .minutesLeft(minutesLeft)
                        .build();
            }
        }
        alarmService.sendFireAlarm(sender, receiver);

        return FireAlarmResponseDto.builder()
                .notifiedAt(now().toString())
                .message("불씨를 지폈어요!")
                .minutesLeft(60)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFriend(Member member1, Member member2) {
        // member1 → member2 관계에 ACCEPTED 상태가 있는지
        boolean direct = friendRepository.existsBySenderAndReceiver(member1, member2);

        // member2 → member1 관계에 ACCEPTED 상태가 있는지
        boolean reverse = friendRepository.existsBySenderAndReceiver(member2, member1);

        return direct || reverse;
    }

}
