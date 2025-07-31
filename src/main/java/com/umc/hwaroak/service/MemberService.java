package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MemberService {

    MemberResponseDto.InfoDto getInfo();

    MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto);

    List<MemberResponseDto.ItemDto> getMyItems();

    MemberResponseDto.ItemDto changeSelectedItem(Long itemId);

    MemberResponseDto.ItemDto findSelectedItem();

    String uploadProfileImage(MultipartFile image);

    void deleteProfileImage();

}
