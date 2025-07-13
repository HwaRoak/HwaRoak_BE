package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.response.KakaoLoginResponseDto;
import com.umc.hwaroak.dto.response.KakaoUserInfoDto;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    private final WebClient webClient = WebClient.create();

    public KakaoLoginResponseDto kakaoLogin(String accessToken) {
        KakaoUserInfoDto userInfo = getUserInfo(accessToken);
        String kakaoId = String.valueOf(userInfo.getId());

        Optional<Member> optionalMember = memberRepository.findByUserId(kakaoId);

        Member member = optionalMember.orElseGet(() -> {
            String email = userInfo.getKakao_account().getEmail();
            String name = userInfo.getKakao_account().getName();
            String nickname = userInfo.getKakao_account().getProfile().getNickname();
            String birthday = userInfo.getKakao_account().getBirthday();
            String profileImage = userInfo.getKakao_account().getProfile().getProfile_image_url();

            return memberRepository.save(new Member(
                    kakaoId, email, name, nickname, birthday, profileImage
            ));
        });

        String jwt = jwtProvider.createToken(member.getId());

        return new KakaoLoginResponseDto(jwt, member.getNickname(), member.getProfileImage(), member.getBirthday());
    }

    private KakaoUserInfoDto getUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoDto.class)
                .block();
    }
}
