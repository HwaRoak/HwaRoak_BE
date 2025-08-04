package com.umc.hwaroak.repository;

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
}
