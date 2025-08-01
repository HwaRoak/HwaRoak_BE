package com.umc.hwaroak.converter;

import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;

import java.util.stream.Collectors;

public class DiaryConverter {

    public static Diary toDiary(Member member, DiaryRequestDto requestDto) {
        return Diary.builder()
                .member(member)
                .recordDate(requestDto.getRecordDate())
                .content(requestDto.getContent())
                .emotionList(requestDto.getEmotionList().stream()
                        .map(Emotion::fromDisplayName)
                        .collect(Collectors.toList()))
                .build();
    }

    public static DiaryResponseDto.CreateDto toCreateDto(Diary diary, String nextItemName) {
        return DiaryResponseDto.CreateDto.builder()
                .id(diary.getId())
                .recordDate(diary.getRecordDate())
                .emotionList(diary.getEmotionList().stream()
                        .map(Emotion::getDisplayName)
                        .collect(Collectors.toList()))
                .feedback(diary.getFeedback())
                .reward(diary.getMember().getReward())
                .memberItemName(nextItemName)
                .build();
    }

    public static DiaryResponseDto.ThumbnailDto toThumbnailDto(Diary diary) {
        return DiaryResponseDto.ThumbnailDto.builder()
                .id(diary.getId())
                .recordDate(diary.getRecordDate())
                .emotionList(diary.getEmotionList().stream()
                        .map(Emotion::getDisplayName)
                        .collect(Collectors.toList()))
                .feedback(diary.getFeedback())
                .build();
    }

    public static DiaryResponseDto.DetailDto toDetailDto(Diary diary) {
        return DiaryResponseDto.DetailDto.builder()
                .id(diary.getId())
                .recordDate(diary.getRecordDate())
                .content(diary.getContent())
                .emotionList(diary.getEmotionList().stream()
                        .map(Emotion::getDisplayName)
                        .collect(Collectors.toList()))
                .build();
    }
}
