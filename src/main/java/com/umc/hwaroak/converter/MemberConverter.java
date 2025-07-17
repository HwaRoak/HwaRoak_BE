package com.umc.hwaroak.converter;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.MemberResponseDto;

public class MemberConverter {
    public static MemberResponseDto.InfoDto toDto(Member member){
        return MemberResponseDto.InfoDto.builder()
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImage())
                .introduction(member.getIntroduction())
                .build();
    }
}
