package com.umc.hwaroak.domain.common;

import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import lombok.Getter;

@Getter
public enum Emotion {
    CALM("차분한"),
    PROUD("뿌듯한"),
    HAPPY("행복한"),
    EXPECTED("기대됨"),
    HEART_FLUTTER("설렘"),
    THANKFUL("감사함"),
    EXCITING("신나는"),
    SADNESS("슬픈"),
    ANGRY("화나는"),
    BORED("무료함"),
    TIRED("피곤함"),
    ANNOYED("짜증남"),
    LONELY("외로움"),
    GLOOMY("우울함"),
    STRESSFUL("스트레스"),;

    private final String displayName;

    Emotion(String displayName) {
        this.displayName = displayName;
    }

    // 한글 이름 찾기
    public static Emotion fromDisplayName(String displayName) {
        for (Emotion emotion: Emotion.values()) {
            if (emotion.getDisplayName().equals(displayName)) {
                return emotion;
            }
        }
        throw new GeneralException(ErrorCode.EMOTION_NOT_FOUND);
    }
}
