package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;

public interface MemberService {

    MemberResponseDto.InfoDto getInfo(Long memberId);

    MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto);


}
