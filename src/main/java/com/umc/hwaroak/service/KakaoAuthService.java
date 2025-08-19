package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.TokenResponseDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;

public interface KakaoAuthService {
    KakaoLoginResponseDto kakaoLogin(String kakaoAccessToken);
    TokenResponseDto reissueTokens(String refreshToken);
}