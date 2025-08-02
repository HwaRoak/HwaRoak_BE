package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findById(Long memberId);

    Optional<Member> findByUserId(String userId);
}
