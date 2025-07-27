package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.AlarmResponseDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface EmitterService {

    // SSE 구독 및 생성
    SseEmitter subscribe(String lastEventId);

    void send(AlarmResponseDto.PreviewDto responseDto);
}
