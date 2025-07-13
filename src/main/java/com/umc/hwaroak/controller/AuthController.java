package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.request.KakaoLoginRequestDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<KakaoLoginResponseDto> kakaoLogin(@RequestBody KakaoLoginRequestDto request) {
        KakaoLoginResponseDto response = kakaoAuthService.kakaoLogin(request.getAccessToken());
        return ResponseEntity.ok(response);
    }
}
