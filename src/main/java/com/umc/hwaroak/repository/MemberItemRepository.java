package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.MemberItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberItemRepository extends JpaRepository<MemberItem, Long> {

    Optional<MemberItem> findByMemberIdAndIsSelectedTrue(Long memberId);

    Optional<MemberItem> findByMemberIdAndItemId(Long memberId, Long itemId);
}
