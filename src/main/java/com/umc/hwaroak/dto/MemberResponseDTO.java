package com.umc.hwaroak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfoDTO{
        Long id;

        String userId;

        String nickname;

        // Todo: 프로필 사진 url

        String introduction;

    }
}
