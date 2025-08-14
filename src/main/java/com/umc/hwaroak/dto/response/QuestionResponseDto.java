package com.umc.hwaroak.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponseDto {
    private String content;
    private String tag;
    private String itemInfo; // 추가
    private String name;     // 추가

    public static QuestionResponseDto of(String content, String tag) {
        return QuestionResponseDto.builder()
                .content(content)
                .tag(tag)
                .itemInfo("")
                .name("")
                .build();
    }

    public static QuestionResponseDto ofReward(String content, String tag, String itemInfo, String name) {
        return QuestionResponseDto.builder()
                .content(content)
                .tag(tag)
                .itemInfo(itemInfo)
                .name(name)
                .build();
    }
}
