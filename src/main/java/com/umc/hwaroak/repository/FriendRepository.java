package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
