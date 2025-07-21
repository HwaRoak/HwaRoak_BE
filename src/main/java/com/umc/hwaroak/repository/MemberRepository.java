package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findById(Long memberId);

    Optional<Member> findByUserId(String userId);

    @Query("SELECT mi FROM MemberItem mi JOIN FETCH mi.item i WHERE mi.member.id = :memberId ORDER BY i.level ASC")
    List<MemberItem> findByMemberIdWithItemOrderedByLevel(Long memberId);

}
