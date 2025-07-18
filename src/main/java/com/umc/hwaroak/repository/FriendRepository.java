package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * sender → receiver 관계가 이미 존재하는지 확인합니다.
     * 요청 중복, 친구 중복 여부 확인 시 사용됩니다.
     *
     * @param sender 요청을 보낸 유저
     * @param receiver 요청을 받은 유저
     * @return 해당 쌍의 Friend 관계가 존재하면 true
     */
    boolean existsBySenderAndReceiver(Member sender, Member receiver);

    /**
     * sender → receiver 방향의 친구 요청을 조회
     * - 수락/거절 시 요청을 식별하기 위한 메서드
     *
     * @param sender 요청 보낸 사람
     * @param receiver 요청 받은 사람 (로그인 유저)
     * @return Friend 엔티티 (없으면 Optional.empty)
     */
    Optional<Friend> findBySenderAndReceiver(Member sender, Member receiver);

    /**
     * 친구 목록 조회 (정확한 ACCEPTED 상태만)
     * - 로그인한 사용자가 sender 또는 receiver인 친구 관계 중
     * - 상태가 정확히 FriendStatus.ACCEPTED 인 것만 반환
     * - status 조건이 OR 조건과 충돌하지 않도록 JPQL로 명시
     *
     * @param member 현재 로그인한 사용자
     * @return 친구 관계 리스트 (ACCEPTED 상태만)
     */
    @Query("SELECT f FROM Friend f " +
            "WHERE (f.sender = :member OR f.receiver = :member) " +
            "AND f.status = com.umc.hwaroak.domain.common.FriendStatus.ACCEPTED")
    List<Friend> findAllAcceptedFriends(@Param("member") Member member);

    /**
     * 받은 친구 요청 목록을 최신순(createdAt DESC)으로 조회
     * 조건: receiver = 현재 유저, status = REQUESTED
     */
    List<Friend> findAllByReceiverAndStatusOrderByCreatedAtDesc(Member receiver, FriendStatus status);

    /**
     * 특정 상태(FriendStatus)를 가진 친구 관계를 조회
     * 예: 친구 삭제 시 ACCEPTED 상태인 관계 찾기
     *
     * @param sender 나 or 상대방
     * @param receiver 상대방 or 나
     * @param status 친구 상태
     * @return Friend 관계 (Optional)
     */
    Optional<Friend> findBySenderAndReceiverAndStatus(Member sender, Member receiver, FriendStatus status);
}
