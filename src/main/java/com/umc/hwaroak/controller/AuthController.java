package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.request.TokenRequestDto;
import com.umc.hwaroak.dto.response.TokenResponseDto;
import com.umc.hwaroak.dto.request.KakaoLoginRequestDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.response.ApiResponse;
import com.umc.hwaroak.response.SuccessCode;
import com.umc.hwaroak.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "카카오 로그인 및 토큰 재발급 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginResponseDto>> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {
        KakaoLoginResponseDto response = kakaoAuthService.kakaoLogin(request.getAccessToken());

// 성공
        return ResponseEntity
                .status(SuccessCode.OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.OK, response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponseDto>> reissue(@RequestBody TokenRequestDto tokenRequest) {
        TokenResponseDto newTokens = kakaoAuthService.reissueTokens(tokenRequest.getRefreshToken());

        return ResponseEntity
                .status(SuccessCode.OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.OK, newTokens));
    }
}
