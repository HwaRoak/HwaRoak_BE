// 안드에게서 access token을 받아서 해당 유저의 정보를 카카오 api통해서 가져오기
// 이미 회원이면 로그인 처리
// 처음이면 회원가입 후 로그인 처리
// JWT 토큰과 유저 정보를 응답으로 반환

package com.umc.hwaroak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.KakaoUserInfoDto;
import com.umc.hwaroak.dto.TokenDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.config.security.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Override
    public KakaoLoginResponseDto kakaoLogin(String kakaoAccessToken) {
        KakaoUserInfoDto kakaoUser = getUserInfoFromKakao(kakaoAccessToken);

        String kakaoId = String.valueOf(kakaoUser.getId());
        KakaoUserInfoDto.KakaoAccount account = kakaoUser.getKakao_account();

        if (account == null || account.getProfile() == null) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        String nickname = account.getProfile().getNickname();
        String profileImage = account.getProfile().getProfile_image_url();

        if (nickname == null || profileImage == null) {
            throw new GeneralException(ErrorCode.TEST_ERROR);
        }

        Optional<Member> optionalMember = memberRepository.findByUserId(kakaoId);

        Member member;
        if (optionalMember.isPresent()) {
            member = optionalMember.get();
            // 테스트용 출력
            System.out.println("✅ 기존 회원입니다: " + member.getNickname());
        } else {
            member = Member.builder()
                    .userId(kakaoId)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .build();
            memberRepository.save(member);
            // 테스트용 출력
            System.out.println("새 회원 가입: " + nickname);
        }


        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        // Redis에 refresh 토큰 저장
        redisTemplate.opsForValue().set(
                "RT:" + member.getId(), refreshToken,
                refreshTokenValidity, TimeUnit.MILLISECONDS
        );

        return KakaoLoginResponseDto.from(accessToken, refreshToken, member);
    }

    @Override
    public TokenDto reissueTokens(TokenDto tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get("RT:" + userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        return new TokenDto(newAccessToken, refreshToken);
    }

    private KakaoUserInfoDto getUserInfoFromKakao(String kakaoAccessToken) {
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
            //  응답 전체를 출력해서 확인(테스트용)
            System.out.println("카카오 응답: " + response.getBody());
            return objectMapper.readValue(response.getBody(), KakaoUserInfoDto.class);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
