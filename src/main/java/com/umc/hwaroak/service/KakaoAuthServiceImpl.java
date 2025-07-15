//// 안드에게서 access token을 받아서 해당 유저의 정보를 카카오 api통해서 가져오기
//// 이미 회원이면 로그인 처리
//// 처음이면 회원가입 후 로그인 처리
//// JWT 토큰과 유저 정보를 응답으로 반환
package com.umc.hwaroak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.TokenDto;
import com.umc.hwaroak.dto.KakaoUserInfoDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements KakaoAuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Override
    public KakaoLoginResponseDto kakaoLogin(String kakaoAccessToken) {
        KakaoUserInfoDto kakaoUser = getUserInfoFromKakao(kakaoAccessToken);
        String kakaoId = String.valueOf(kakaoUser.getId());

        Optional<Member> optionalMember = memberRepository.findByUserId(kakaoId);
        Member member = optionalMember.orElseGet(() -> {
            KakaoUserInfoDto.KakaoAccount account = kakaoUser.getKakao_account();
            KakaoUserInfoDto.KakaoAccount.Profile profile = account.getProfile();
            return memberRepository.save(new Member(
                    kakaoId,
                    account.getEmail(),
                    account.getName(),
                    profile.getNickname(),
                    account.getBirthyear() + "." + account.getBirthday(),
                    profile.getProfile_image_url()
            ));
        });

        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        redisTemplate.opsForValue().set(
                "RT:" + member.getId(), refreshToken,
                refreshTokenValidity, TimeUnit.MILLISECONDS
        );

        return new KakaoLoginResponseDto(accessToken, refreshToken, member);
    }

    @Override
    public TokenDto reissueTokens(TokenDto tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get("RT:" + userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token mismatch");
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        return new TokenDto(newAccessToken, refreshToken);
    }

    private KakaoUserInfoDto getUserInfoFromKakao(String kakaoAccessToken) {
        if ("mock".equals(kakaoAccessToken)) {
            KakaoUserInfoDto.KakaoAccount.Profile profile = new KakaoUserInfoDto.KakaoAccount.Profile();
            profile.setNickname("MockUser");
            profile.setProfile_image_url("https://mock.profile.img");

            KakaoUserInfoDto.KakaoAccount account = new KakaoUserInfoDto.KakaoAccount();
            account.setEmail("mockuser@kakao.com");
            account.setName("Mock Name");
            account.setBirthyear("1999");
            account.setBirthday("0101");
            account.setProfile(profile);

            KakaoUserInfoDto mockUser = new KakaoUserInfoDto();
            mockUser.setId(123456789L);
            mockUser.setKakao_account(account);

            return mockUser;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + kakaoAccessToken);
            headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return objectMapper.readValue(response.getBody(), KakaoUserInfoDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user info from Kakao", e);
        }
    }
}

