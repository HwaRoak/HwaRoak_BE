package com.umc.hwaroak.repository.MemberRepository;

import com.umc.hwaroak.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

}
