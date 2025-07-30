package com.umc.hwaroak.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EmitterRepository {

    void put(String emitterId, SseEmitter sseEmitter);

    Optional<SseEmitter> get(String key);

    void remove(String key);

    Map<String, List<SseEmitter>> findAllEmittersStartWithByMemberId(String memberId);
}