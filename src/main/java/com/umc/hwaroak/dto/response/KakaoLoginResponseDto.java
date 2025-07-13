// 백엔드가 로그인 처리후 클라이언트에게 줄 응답
// 로그인 성공시 -> JWT 토큰 + 사용자 닉네임으로 응답을 줌
package com.umc.hwaroak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginResponseDto {
    private String jwtToken;
    private String nickname;
    private String profileImage;
    private String birthday;
}

