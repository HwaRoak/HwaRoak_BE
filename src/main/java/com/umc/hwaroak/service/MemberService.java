package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;

public interface MemberService {

    MemberResponseDto.InfoDto getInfo();
    MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto);


}
