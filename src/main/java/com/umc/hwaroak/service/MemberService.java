package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    MemberResponseDto.InfoDto getInfo();

    MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto);

    MemberResponseDto.ProfileImageDto uploadProfileImage(MultipartFile image);

    MemberResponseDto.ProfileImageDto deleteProfileImage();

    MemberResponseDto.PreviewDto getMyPagePreview();
}
