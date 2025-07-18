package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.TokenDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;

public interface KakaoAuthService {
    KakaoLoginResponseDto kakaoLogin(String kakaoAccessToken);
    TokenDto reissueTokens(TokenDto tokenRequest);
}