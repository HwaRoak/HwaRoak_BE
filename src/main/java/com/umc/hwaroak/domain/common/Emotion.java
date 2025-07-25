package com.umc.hwaroak.domain.common;

import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import lombok.Getter;

@Getter
public enum Emotion {
    CALM("차분한", EmotionCategory.CALM),
    PROUD("뿌듯한", EmotionCategory.CALM),
    THANKFUL("감사함", EmotionCategory.CALM),

    HAPPY("행복한", EmotionCategory.HAPPY),
    EXPECTED("기대됨", EmotionCategory.HAPPY),
    HEART_FLUTTER("설렘", EmotionCategory.HAPPY),
    EXCITING("신나는", EmotionCategory.HAPPY),

    BORED("무료함", EmotionCategory.SAD),
    LONELY("외로움", EmotionCategory.SAD),
    GLOOMY("우울함", EmotionCategory.SAD),
    SADNESS("슬픈", EmotionCategory.SAD),

    ANGRY("화나는", EmotionCategory.ANGRY),
    ANNOYED("짜증남", EmotionCategory.ANGRY),
    STRESSFUL("스트레스", EmotionCategory.ANGRY),
    TIRED("피곤함", EmotionCategory.ANGRY);

    private final String displayName;
    private final EmotionCategory category;

    Emotion(String displayName, EmotionCategory category) {
        this.displayName = displayName;
        this.category = category;
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
