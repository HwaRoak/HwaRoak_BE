package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.MemberResponseDto;

public interface MemberService {

    MemberResponseDto.InfoDto getInfo(Long memberId);
}
