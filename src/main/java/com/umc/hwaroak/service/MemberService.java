package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    MemberResponseDto.InfoDto getInfo();

    MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto);

    MemberResponseDto.PresignedUrlResponseDto createPresignedUrl(MemberRequestDto.PresignedUrlRequestDto request);

    MemberResponseDto.ProfileImageConfirmResponseDto confirmProfileImage(MemberRequestDto.ProfileImageConfirmRequestDto request);

    MemberResponseDto.ProfileImageConfirmResponseDto deleteProfileImage();

    MemberResponseDto.PreviewDto getMyPagePreview();
}
