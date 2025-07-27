package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Alarm;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface EmitterService {

    // SSE 구독 및 생성
    SseEmitter subscribe(String lastEventId);

    void send(Long memberId, Alarm alarm);

    void sendNotification(String redisKey, Alarm alarm);
}
