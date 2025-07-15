// 카카오 access token으로 사용자 정보를 가져올 때 응답받기 위한 dto
// 클라이언트에게 주는 응답아님

package com.umc.hwaroak.dto;

import lombok.Getter;
import lombok.Setter;
//@Getter
//public class KakaoUserInfoDto {
//    private String id;
//    private Profile profile;
//
//    @Getter @Setter
//    public static class Profile {
//        private String nickname;
//        private String profileImageUrl;
//    }
//}
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserInfoDto {
    private Long id;
    private KakaoAccount kakao_account;

    @Getter
    @Setter
    public static class KakaoAccount {
        private Profile profile;
    }

    @Getter
    @Setter
    public static class Profile {
        private String nickname;
        private String profile_image_url;
    }
}



