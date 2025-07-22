package com.umc.hwaroak.domain.common;

public enum MainMessageType {
    REWARD_HINT,         // "보상을 받아봐!" 고정
    REWARD_BY_LEVEL,     // itemLevel별 보상 수령 후 메시지
    FIRE_ALERT,          // 불씨 메시지 랜덤
    DIARY_EMPTY,         // 일기 안 쓴 경우
    EMOTION_MESSAGE      // 감정 메시지 (DB 저장 X, GPT 결과)
}
