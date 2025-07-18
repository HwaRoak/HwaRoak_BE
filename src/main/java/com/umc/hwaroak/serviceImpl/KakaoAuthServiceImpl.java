//// 안드에게서 access token을 받아서 해당 유저의 정보를 카카오 api통해서 가져오기
//// 이미 회원이면 로그인 처리
//// 처음이면 회원가입 후 로그인 처리
//// JWT 토큰과 유저 정보를 응답으로 반환

package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.response.KakaoUserInfoDto;
import com.umc.hwaroak.dto.response.TokenDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.authentication.JwtTokenProvider;
import com.umc.hwaroak.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements KakaoAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final WebClient.Builder webClientBuilder;  // WebClient는 Builder로 주입받음

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

        Member member = memberRepository.findByUserId(kakaoId)
                .orElseGet(() -> {
                    Member newMember = new Member(kakaoId, nickname, profileImage);
                    return memberRepository.save(newMember);
                });

        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

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
            return webClientBuilder
                    .baseUrl("https://kapi.kakao.com")
                    .build()
                    .get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("카카오 API 오류: " + body)))
                    .bodyToMono(KakaoUserInfoDto.class)
                    .block();  // 동기식 처리

        } catch (Exception e) {
            throw new RuntimeException("카카오 유저 정보를 불러오지 못했습니다.", e);
        }
    }
}
