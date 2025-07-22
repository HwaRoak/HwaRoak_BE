package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.response.MainMessageResponseDto;

public interface MainMessageService {

    /**
     * 메인 화면에 보여줄 메시지를 조건 우선순위에 따라 반환합니다.
     * 우선순위:
     * 1. 리워드 수령 가능 여부 (미구현)
     * 2. 읽지 않은 불씨 알람 존재 여부
     * 3. 오늘 일기 미작성 여부
     * 4. 일기 작성 + 감정 분석 완료 (feedback)
     *
     * @return 메인 메시지 문자열
     */
    MainMessageResponseDto getMainMessage(Member member);
}
