package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.FriendStatus;
import com.umc.hwaroak.dto.response.FireAlarmResponseDto;
import com.umc.hwaroak.dto.response.FriendResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.FriendRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmService;
import com.umc.hwaroak.service.FriendService;
import com.umc.hwaroak.util.OpenAiUtil;
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
    private final DiaryRepository diaryRepository;
    private final AlarmService alarmService;
    private final OpenAiUtil openAiUtil;

    /**
     * 친구 요청을 보냅니다.
     * 요청 전에 다음 조건을 확인합니다:
     * - 존재하지 않는 유저에게 요청 ❌
     * - 자기 자신에게 요청 ❌
     * - 이미 요청했거나 친구 상태인 경우 ❌
     * 조건을 만족하면 Friend 엔티티를 저장합니다.
     *
     * @param receiverUserId 친구 요청을 받을 Member의 ID
     */
    @Override
    @Transactional
    public void requestFriend(String receiverUserId) {
        // [1] 현재 로그인된 유저를 가져옵니다.
        Member sender = memberLoader.getMemberByContextHolder();

        // [2] 요청받을 유저를 userId 기준으로 조회
        Member receiver = memberRepository.findByUserId(receiverUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        // [3] 자기 자신에게 친구 요청하는 경우 예외 처리
        if (sender.getId().equals(receiver.getId())) {
            throw new GeneralException(ErrorCode.CANNOT_ADD_SELF);
        }

        // [4] 기존 친구 요청/관계가 존재하는지 조회 (단방향 sender → receiver 기준)
        Optional<Friend> existingFriend = friendRepository.findBySenderAndReceiver(sender, receiver);

        if (existingFriend.isPresent()) {
            Friend friend = existingFriend.get();

            if (friend.getStatus() == FriendStatus.BLOCKED || friend.getStatus() == FriendStatus.REJECTED) {
                friend.updateStatus(FriendStatus.REQUESTED);
                return;
            }

            throw new GeneralException(ErrorCode.FRIEND_ALREADY_EXISTS_OR_REQUESTED);
        }

        // [5] 역방향 중복 검사
        boolean reverseExists = friendRepository.existsBySenderAndReceiver(receiver, sender);
        if (reverseExists) {
            throw new GeneralException(ErrorCode.FRIEND_ALREADY_EXISTS_OR_REQUESTED);
        }

        // [6] 저장 및 알림 전송
        Friend friend = new Friend(sender, receiver, FriendStatus.REQUESTED);
        friendRepository.save(friend);
        alarmService.sendFriendRequestAlarm(sender, receiver);
    }



    @Override
    @Transactional
    public void acceptFriendRequest(String senderUserId) {
        Member receiver = memberLoader.getMemberByContextHolder();

        Member sender = memberRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        Friend friendRequest = friendRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (friendRequest.getStatus() != FriendStatus.REQUESTED) {
            throw new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
        }

        friendRequest.updateStatus(FriendStatus.ACCEPTED);
    }



    @Override
    @Transactional
    public void rejectFriendRequest(String senderUserId) {
        Member receiver = memberLoader.getMemberByContextHolder();

        Member sender = memberRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        Friend friendRequest = friendRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (friendRequest.getStatus() != FriendStatus.REQUESTED) {
            throw new GeneralException(ErrorCode.FRIEND_REQUEST_NOT_PENDING);
        }

        friendRequest.updateStatus(FriendStatus.REJECTED);
    }


    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto.FriendInfo> getFriendList() {
        Member me = memberLoader.getMemberByContextHolder();

        List<Friend> acceptedFriends = friendRepository.findAllAcceptedFriends(me);

        return acceptedFriends.stream()
                .map(friend -> {
                    Member friendMember = friend.getSender().equals(me)
                            ? friend.getReceiver()
                            : friend.getSender();

                    return FriendResponseDto.FriendInfo.builder()
                            .userId(friendMember.getUserId())
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

        List<Friend> requests = friendRepository.findAllByReceiverAndStatusOrderByCreatedAtDesc(
                me, FriendStatus.REQUESTED
        );

        return requests.stream()
                .map(friend -> {
                    Member sender = friend.getSender();
                    return FriendResponseDto.ReceivedRequestInfo.builder()
                            .userId(sender.getUserId())
                            .nickname(sender.getNickname())
                            .introduction(sender.getIntroduction())
                            .build();
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void deleteFriend(String friendUserId) {
        Member me = memberLoader.getMemberByContextHolder();
        Member friend = memberRepository.findByUserId(friendUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        Friend relationship = friendRepository.findBySenderAndReceiverAndStatus(me, friend, FriendStatus.ACCEPTED)
                .or(() -> friendRepository.findBySenderAndReceiverAndStatus(friend, me, FriendStatus.ACCEPTED))
                .orElseThrow(() -> new GeneralException(ErrorCode.FRIEND_NOT_FOUND));

        if (relationship.getStatus() != FriendStatus.ACCEPTED) {
            throw new GeneralException(ErrorCode.FRIEND_CANNOT_BE_DELETED);
        }

        relationship.updateStatus(FriendStatus.BLOCKED);
    }


    @Override
    @Transactional(readOnly = true)
    public FriendResponseDto.SearchResultDto searchFriendByUserId(String userId) {
        Member currentMember = memberLoader.getMemberByContextHolder();

        Member target = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getId().equals(target.getId())) {
            throw new GeneralException(ErrorCode.CANNOT_SEARCH_SELF);
        }

        return FriendResponseDto.SearchResultDto.builder()
                .userId(target.getUserId())
                .nickname(target.getNickname())
                .introduction(target.getIntroduction())
                .build();
    }


    @Override
    @Transactional
    public FireAlarmResponseDto fireFriend(String friendUserId) {
        Member sender = memberLoader.getMemberByContextHolder();
        Member receiver = memberRepository.findByUserId(friendUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        if (!isFriend(sender, receiver)) {
            throw new GeneralException(ErrorCode.NOT_FRIEND);
        }

        Optional<LocalDateTime> lastFireTimeOpt = alarmService.getLastFireTime(sender, receiver);
        LocalDateTime now = LocalDateTime.now();

        if (lastFireTimeOpt.isPresent()) {
            long minutesPassed = Duration.between(lastFireTimeOpt.get(), now).toMinutes();
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
    public FriendResponseDto.FriendPageInfo getFriendPage(String friendUserId) {
        Member friend = memberRepository.findByUserId(friendUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        Optional<Diary> diaryOpt = diaryRepository
                .findTop1ByMemberAndRecordDateAfterOrderByRecordDateDesc(friend, threeDaysAgo);

        String message = diaryOpt
                .map(diary -> openAiUtil.extractDiaryFeelingSummary(diary.getContent()))
                .orElse("불씨를 지펴보세요!");

        return FriendResponseDto.FriendPageInfo.builder()
                .userId(friend.getUserId())
                .nickname(friend.getNickname())
                .message(message)
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
