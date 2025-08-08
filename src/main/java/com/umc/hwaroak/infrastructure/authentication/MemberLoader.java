package com.umc.hwaroak.infrastructure.authentication;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberLoader {

    private final MemberRepository memberRepository;

    public Long getCurrentMemberId() {
        return getMemberByContextHolder().getId();
    }

    public Member getMemberByContextHolder() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        Object principal = authentication.getPrincipal();
        Long memberId = Long.parseLong((String) principal);

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
