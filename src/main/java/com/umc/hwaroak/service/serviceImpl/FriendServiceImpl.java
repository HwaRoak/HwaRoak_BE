package com.umc.hwaroak.service.serviceImpl;

import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.event.FireSendEvent;
import com.umc.hwaroak.event.FriendRequestEvent;
import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.domain.common.FriendStatus;
import com.umc.hwaroak.dto.response.FireAlarmResponseDto;
import com.umc.hwaroak.dto.response.FriendResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.FriendRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmService;
import com.umc.hwaroak.service.FriendService;
import com.umc.hwaroak.util.OpenAiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final MemberLoader memberLoader;
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;

    private final AlarmService alarmService;

    private final OpenAiUtil openAiUtil;

    private final ApplicationEventPublisher eventPublisher;

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
        Member sender = memberLoader.getMemberByContextHolder();
        Member receiver = memberRepository.findByUserId(receiverUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        if (sender.getId().equals(receiver.getId())) {
            throw new GeneralException(ErrorCode.CANNOT_ADD_SELF);
        }

        Optional<Friend> directOpt = friendRepository.findBySenderAndReceiver(sender, receiver);
        Optional<Friend> reverseOpt = friendRepository.findBySenderAndReceiver(receiver, sender);

        // 내가 예전에 보낸 적 있음
        if (directOpt.isPresent()) {
            Friend f = directOpt.get();
            switch (f.getStatus()) {
                case BLOCKED, REJECTED  -> {
                    f.updateStatus(FriendStatus.REQUESTED); // 재요청 허용
                    eventPublisher.publishEvent(new FriendRequestEvent(this, sender, receiver));
                    return;
                }
                case REQUESTED -> throw new GeneralException(ErrorCode.FRIEND_ALREADY_REQUESTED);
                case ACCEPTED  -> throw new GeneralException(ErrorCode.FRIEND_ALREADY_FRIENDS);
            }
        }

        // 상대가 과거에 나에게 보낸 기록 있음
        if (reverseOpt.isPresent()) {
            Friend f = reverseOpt.get();
            switch (f.getStatus()) {
                case BLOCKED, REJECTED -> {
                    // 방향 반전 + 재요청
                    f.setSender(sender);
                    f.setReceiver(receiver);
                    f.updateStatus(FriendStatus.REQUESTED);
                    eventPublisher.publishEvent(new FriendRequestEvent(this, sender, receiver));
                    return;
                }
                case REQUESTED -> throw new GeneralException(ErrorCode.FRIEND_ALREADY_REQUESTED);
                case ACCEPTED  -> throw new GeneralException(ErrorCode.FRIEND_ALREADY_FRIENDS);
            }
        }

        // 신규 생성
        Friend friend = new Friend(sender, receiver, FriendStatus.REQUESTED);
        friendRepository.save(friend);
        eventPublisher.publishEvent(new FriendRequestEvent(this, sender, receiver));
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
                            .profileImage(friendMember.getProfileImage())
                            .build();
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<FriendResponseDto.FriendInfo> getReceivedFriendRequests() {
        Member me = memberLoader.getMemberByContextHolder();

        List<Friend> requests = friendRepository.findAllByReceiverAndStatusOrderByCreatedAtDesc(
                me, FriendStatus.REQUESTED
        );

        return requests.stream()
                .map(friend -> {
                    Member sender = friend.getSender();
                    return FriendResponseDto.FriendInfo.builder()
                            .userId(sender.getUserId())
                            .nickname(sender.getNickname())
                            .introduction(sender.getIntroduction())
                            .profileImage(sender.getProfileImage())
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
    public FriendResponseDto.FriendInfo searchFriendByUserId(String userId) {
        Member currentMember = memberLoader.getMemberByContextHolder();

        Member target = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getId().equals(target.getId())) {
            throw new GeneralException(ErrorCode.CANNOT_SEARCH_SELF);
        }

        return FriendResponseDto.FriendInfo.builder()
                .userId(target.getUserId())
                .nickname(target.getNickname())
                .introduction(target.getIntroduction())
                .profileImage(target.getProfileImage())
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

        eventPublisher.publishEvent(new FireSendEvent(this, sender, receiver));

        return FireAlarmResponseDto.builder()
                .notifiedAt(now().toString())
                .message("불씨를 지폈어요!")
                .minutesLeft(60)
                .build();
    }

@Transactional(readOnly = true)
public FriendResponseDto.FriendPageInfo getFriendPage(String friendUserId) {
    log.info("[getFriendPage] 친구 페이지 조회 시작 - friendUserId: {}", friendUserId);

    Member friend = memberRepository.findByUserId(friendUserId)
            .orElseThrow(() -> {
                log.warn("[getFriendPage] 해당 userId의 친구를 찾을 수 없음: {}", friendUserId);
                return new GeneralException(ErrorCode.MEMBER_NOT_FOUND);
            });
    log.info("[getFriendPage] 친구 조회 완료 - memberId: {}, nickname: {}", friend.getId(), friend.getNickname());

    // 최근 3일 내 최신 일기 조회 (LocalDate 기준)
    LocalDate threeDaysAgo = LocalDateTime.now().minusDays(3).toLocalDate();
    log.info("[getFriendPage] 최근 3일 기준 날짜: {}", threeDaysAgo);

    Optional<Diary> diaryOpt = diaryRepository
            .findTop1ByMemberAndRecordDateGreaterThanEqualOrderByRecordDateDesc(friend, threeDaysAgo);

    // GPT 분석 멘트
    String message = diaryOpt
            .map(diary -> {
                log.info("[getFriendPage] 최신 일기 ID: {}, 작성일: {}", diary.getId(), diary.getRecordDate());
                return openAiUtil.extractDiaryFeelingSummary(diary.getContent());
            })
            .orElse("불씨를 지펴보세요!");

    // 감정 ENUM → message 만든 일기에서 가져오기 (없으면 "")
    String emotions = diaryOpt
            .map(d -> {
                List<Emotion> list = d.getEmotionList();
                if (list == null || list.isEmpty()) return "";
                return list.stream()
                        .map(Emotion::getDisplayName)
                        .collect(Collectors.joining(","));
            })
            .orElse("");

    // 선택된 MemberItem의 Item PK
    Long selectedItemId = friend.getMemberItemList().stream()
            .filter(MemberItem::getIsSelected)
            .findFirst()
            .map(mi -> mi.getItem().getId())
            .orElse(null);

    log.info("[getFriendPage] 반환 메시지: {}", message);

    FriendResponseDto.FriendPageInfo result = FriendResponseDto.FriendPageInfo.builder()
            .userId(friend.getUserId())
            .nickname(friend.getNickname())
            .message(message)
            .emotions(emotions)
            .selectedItemId(selectedItemId)
            .build();

    log.info("[getFriendPage] 최종 응답: {}", result);
    return result;
}


    @Transactional(readOnly = true)
    public FriendResponseDto.FriendItemsInfo getFriendItems(String friendUserId) {
        log.info("[getFriendItems] 친구 아이템 리스트 조회 시작 - friendUserId: {}", friendUserId);

        Member friend = memberRepository.findByUserId(friendUserId)
                .orElseThrow(() -> {
                    log.warn("[getFriendItems] 해당 userId의 친구를 찾을 수 없음: {}", friendUserId);
                    return new GeneralException(ErrorCode.MEMBER_NOT_FOUND);
                });
        log.info("[getFriendItems] 친구 조회 완료 - memberId: {}, nickname: {}", friend.getId(), friend.getNickname());

        // isReceived == true 인 아이템만 필터링
        List<Long> items = friend.getMemberItemList().stream()
                .filter(MemberItem::getIsReceived)
                .map(mi -> mi.getItem().getId())
                .collect(Collectors.toList());
        log.info("[getFriendItems] 보유 Item PK 리스트 (isReceived=true): {}", items);

        // 선택된 아이템도 isReceived == true 인 것만
        Long selectedItem = friend.getMemberItemList().stream()
                .filter(MemberItem::getIsReceived)
                .filter(MemberItem::getIsSelected)
                .findFirst()
                .map(mi -> mi.getItem().getId())
                .orElse(null);
        log.info("[getFriendItems] 선택된 Item PK (isReceived=true): {}", selectedItem);

        FriendResponseDto.FriendItemsInfo result = FriendResponseDto.FriendItemsInfo.builder()
                .items(items)
                .selectedItem(selectedItem)
                .build();

        log.info("[getFriendItems] 최종 응답: {}", result);
        return result;
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