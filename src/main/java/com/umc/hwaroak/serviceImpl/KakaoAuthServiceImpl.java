//// 안드에게서 access token을 받아서 해당 유저의 정보를 카카오 api통해서 가져오기
//// 이미 회원이면 로그인 처리
//// 처음이면 회원가입 후 로그인 처리
//// JWT 토큰과 유저 정보를 응답으로 반환

package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.domain.Item;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.KakaoUserInfoDto;
import com.umc.hwaroak.dto.response.TokenDto;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.ItemRepository;
import com.umc.hwaroak.repository.MemberItemRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.authentication.JwtTokenProvider;
import com.umc.hwaroak.service.KakaoAuthService;
import com.umc.hwaroak.util.UidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements KakaoAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtProvider;
    private final StringRedisTemplate stringRedisTemplate;
    private final UidGenerator uidGenerator;
    private final WebClient.Builder webClientBuilder;  // WebClient는 Builder로 주입받음
    private final ItemRepository itemRepository;
    private final MemberItemRepository memberItemRepository;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Override
    public KakaoLoginResponseDto kakaoLogin(String kakaoAccessToken) {
        log.info("카카오 로그인 시도 - AccessToken: {}", kakaoAccessToken);

        KakaoUserInfoDto kakaoUser = getUserInfoFromKakao(kakaoAccessToken);

        String uid = uidGenerator.generateShortUid(String.valueOf(kakaoUser.getId()));
        KakaoUserInfoDto.KakaoAccount account = kakaoUser.getKakao_account();

        log.info("Added uid : {}", uid);
        if (account == null || account.getProfile() == null) {
            throw new GeneralException(ErrorCode.MEMBER_NOT_FOUND);
        }

        String nickname = account.getProfile().getNickname();
        String profileImage = account.getProfile().getProfile_image_url();
        log.info("카카오 유저 정보 조회 완료 - kakaoId: {}, nickname: {}", kakaoUser.getId(), nickname);

        Member member = memberRepository.findByUserId(uid)
                .orElseGet(() -> {

                    // 신규 가입
                    Member newMember = new Member(uid, nickname, profileImage);
                    log.info("신규 회원 가입 - userId: {}", uid);
                    Member savedNewMember = memberRepository.save(newMember);

                    // 기본 아이템 설정
                    Item defaultItem = itemRepository.findByLevel(1)
                            .orElseThrow(() -> new GeneralException(ErrorCode.ITEM_NOT_FOUND));
                    MemberItem newMemberItem = MemberItem.builder()
                            .member(savedNewMember)
                            .item(defaultItem)
                            .isSelected(true)
                            .build();

                    memberItemRepository.save(newMemberItem);

                    return savedNewMember;
                });

        // redis에서 현재 발급된 refresh token확인하기
        String existingRefreshToken = stringRedisTemplate.opsForValue().get("RT:" + member.getId());
        log.debug("기존 Refresh Token 조회 결과 - token: {}", existingRefreshToken);

        String accessToken;
        String refreshToken;

        if (existingRefreshToken != null && jwtProvider.validateToken(existingRefreshToken)) {
            log.info("기존 Refresh Token 유효 - 재사용");
            // refresh token 유효 -> 재사용
            refreshToken = existingRefreshToken;
        } else {
            // 없거나 만료 -> 새로 발급
            log.info("Refresh Token 없음 또는 만료 - 새 발급");
            refreshToken = jwtProvider.createRefreshToken(member.getId());
            stringRedisTemplate.opsForValue().set(
                    "RT:" + member.getId(), refreshToken,
                    refreshTokenValidity, TimeUnit.MILLISECONDS
            );
            log.debug("새 Refresh Token 저장 완료 - ttl: {}ms", refreshTokenValidity);
        }

        accessToken = jwtProvider.createAccessToken(member.getId());
        log.debug("AccessToken 발급 완료: {}", accessToken);

        log.info("카카오 로그인 완료 - memberId: {}", member.getId());
        return KakaoLoginResponseDto.from(accessToken, refreshToken, member);

    }

    @Override
    public TokenDto reissueTokens(TokenDto tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String memberId = jwtProvider.getMemberId(refreshToken);
        String storedRefreshToken = stringRedisTemplate.opsForValue().get("RT:" + memberId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtProvider.createAccessToken(Long.parseLong(memberId));
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
            log.warn("Failed Profile... : {}", e.getMessage());
            throw new GeneralException(ErrorCode.FAILED_KAKAO_PROFILE);
        }
    }
}
