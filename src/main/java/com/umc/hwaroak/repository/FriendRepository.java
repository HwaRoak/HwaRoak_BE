package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Friend;
import com.umc.hwaroak.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
