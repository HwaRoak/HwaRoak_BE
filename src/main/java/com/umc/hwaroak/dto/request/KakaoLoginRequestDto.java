// Android에서 받은 access token을 백엔드로 전달할 때 사용하는 요청 형식
// 클라이언트 -> 서버
package com.umc.hwaroak.dto.request;

import lombok.Getter;

@Getter
public class KakaoLoginRequestDto {
    private String accessToken;
}

