package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.TokenDto;
import com.umc.hwaroak.dto.request.KakaoLoginRequestDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.service.KakaoAuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "카카오 로그인 및 JWT 토큰 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthServiceImpl kakaoAuthService;

    @Operation(summary = "카카오 로그인", description = "카카오 accessToken으로 JWT access/refresh 토큰을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "JWT 토큰 발급 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 카카오 accessToken (INVALID_KAKAO_TOKEN)")
    @PostMapping("/kakao")
    public ResponseEntity<KakaoLoginResponseDto> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {
        KakaoLoginResponseDto response = kakaoAuthService.kakaoLogin(request.getAccessToken());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 재발급", description = "refreshToken을 이용해 accessToken을 재발급합니다.")
    @ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공")
    @ApiResponse(responseCode = "400", description = "유효하지 않거나 만료된 refreshToken (INVALID_REFRESH_TOKEN)")
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenDto tokenRequest) {
        TokenDto newTokens = kakaoAuthService.reissueTokens(tokenRequest);
        return ResponseEntity.ok(newTokens);
    }
}
