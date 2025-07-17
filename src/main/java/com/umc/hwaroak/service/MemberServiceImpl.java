package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.MemberResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;

    @Override
    public MemberResponseDto.InfoDto getInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.InfoDto.builder()
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .introduction(member.getIntroduction())
                .build();
    }
}
