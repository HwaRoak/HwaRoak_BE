package com.umc.hwaroak.repository.MemberRepository;

import com.umc.hwaroak.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    Optional<Member> findByUserId(String userId);

}
