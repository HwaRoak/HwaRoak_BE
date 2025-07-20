package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.TokenDto;
import com.umc.hwaroak.dto.request.KakaoLoginRequestDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.response.ApiResponse;
import com.umc.hwaroak.response.SuccessCode;
import com.umc.hwaroak.serviceImpl.KakaoAuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "카카오 로그인 및 토큰 재발급 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthServiceImpl kakaoAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginResponseDto>> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {
        KakaoLoginResponseDto response = kakaoAuthService.kakaoLogin(request.getAccessToken());

// 성공
        return ResponseEntity
                .status(SuccessCode.OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.OK, response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenDto>> reissue(@RequestBody TokenDto tokenRequest) {
        TokenDto newTokens = kakaoAuthService.reissueTokens(tokenRequest);

        return ResponseEntity
                .status(SuccessCode.OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.OK, newTokens));
    }
}
