package com.umc.hwaroak.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // SSE에 대한 기본적인 쿼리

    // 추가
    @Override
    public void put(String key, SseEmitter sseEmitter) {
        emitters.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(sseEmitter);
    }
    // 조회
    @Override
    public Optional<SseEmitter> get(String key) {
        List<SseEmitter> list = emitters.get(key);
        if (list != null && !list.isEmpty()) {
            return Optional.of(list.get(list.size() - 1)); // 가장 최근 추가된 emitter 반환
        }
        return Optional.empty();
    }

    // 삭제
    @Override
    public void remove(String key) {
        emitters.remove(key);
    }

    // MemberId 기반
    @Override
    public Map<String, List<SseEmitter>> findAllEmittersStartWithByMemberId(String memberId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(memberId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
