package com.umc.hwaroak.converter;

import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;

public class DiaryConverter {

    public static Diary toDiary(Member member, DiaryRequestDto requestDto) {
        return Diary.builder()
                .member(member)
                .recordDate(requestDto.getRecordDate())
                .content(requestDto.getContent())
                .emotion(requestDto.getEmotion())
                .build();
    }

    public static DiaryResponseDto toDto(Diary diary) {
        return DiaryResponseDto.builder()
                .id(diary.getId())
                .feedback(diary.getFeedback())
                .reward(diary.getMember().getReward())
                .build();
    }
}
