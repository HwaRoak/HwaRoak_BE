package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.TokenDto;
import com.umc.hwaroak.dto.request.KakaoLoginRequestDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.response.ApiResponse;
import com.umc.hwaroak.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "카카오 로그인 및 JWT 토큰 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final com.umc.hwaroak.service.KakaoAuthServiceImpl kakaoAuthService;

    @Operation(summary = "카카오 로그인", description = "카카오 accessToken으로 JWT access/refresh 토큰을 발급합니다.")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginResponseDto>> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {
        KakaoLoginResponseDto response = kakaoAuthService.kakaoLogin(request.getAccessToken());
        ApiResponse<KakaoLoginResponseDto> result =
                (ApiResponse<KakaoLoginResponseDto>) ApiResponse.of(SuccessCode.OK, response);

        return ResponseEntity.ok(result);    }

    @Operation(summary = "토큰 재발급", description = "refreshToken을 이용해 accessToken을 재발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenDto>> reissue(@RequestBody TokenDto tokenRequest) {
        TokenDto newTokens = kakaoAuthService.reissueTokens(tokenRequest);
        return ResponseEntity.ok((ApiResponse<TokenDto>) ApiResponse.of(SuccessCode.OK, newTokens));
    }
}
