package com.umc.hwaroak.authentication;

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
        String userId;

        if (principal instanceof String) {
            userId = (String) principal;
        } else {
            throw new GeneralException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
