package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberItemRepository extends JpaRepository<MemberItem, Long>, ItemRepositoryCustom {

    Optional<MemberItem> findByMemberIdAndIsSelectedTrue(Long memberId);

    @Query("SELECT mi FROM MemberItem mi WHERE mi.id = :memberItemId")
    Optional<MemberItem> findById(Long memberItemId);

    @Query("SELECT mi FROM MemberItem mi JOIN FETCH mi.item i WHERE mi.member.id = :memberId and mi.isReceived = true ORDER BY i.level ASC")
    List<MemberItem> findByMemberIdWithItemOrderedByLevel(Long memberId);

    // isReceived = false 인거 가져왔을 때 있다? -> 보상 받을 수 있는 상황.
    @Query("SELECT mi FROM MemberItem mi WHERE mi.member = :member AND mi.isReceived = false")
    List<MemberItem> findUnreceivedItems(Member member);

}
