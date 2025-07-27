package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.EmitterRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.EmitterService;
import com.umc.hwaroak.util.SseRepositoryKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmitterServiceImpl implements EmitterService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final EmitterRepository emitterRepository;
    private final MemberLoader memberLoader;

    @Override
    public SseEmitter subscribe(String lastEventId) {

        LocalDateTime now = LocalDateTime.now();
        Long memberId = memberLoader.getCurrentMemberId();
        String key = new SseRepositoryKeyGenerator(memberId, AlarmType.CONNECTED, now)
                .toCompleteKeyWhichSpecifyOnlyOneValue();

        SseEmitter sse = new SseEmitter(Long.MAX_VALUE);

        // 검사
        emitters.computeIfAbsent(key, e -> new ArrayList<>())
                .add(sse);
        sse.onCompletion(() -> {
            log.info("onCompletion callback : {}", key);
            emitterRepository.remove(key);
        });
        sse.onTimeout(() -> {
            log.info("onTimeout callback");
            sse.complete();
        });
        sse.onError((Throwable th) -> {
            emitterRepository.remove(key);
            log.error("Emitter eorror for key: {} with {}", key, th.getMessage());
        });

        // 새로 등록 저장
        emitterRepository.put(key, sse);
        try {
            sse.send(SseEmitter.event()
                    .name(AlarmType.CONNECTED.getValue())
                    .id(getEventId(memberId, now, AlarmType.CONNECTED))
                    .data("subscribe")
            );
        } catch (IOException e) {
            emitterRepository.remove(key);
            log.info("SSE Exception: {}", e.getMessage());
            throw new GeneralException(ErrorCode.SSE_CONNECTION_ERROR);
        }
        return sse;
    }


    @Override
    public void send(Long memberId, Alarm alarm) {

        String key = new SseRepositoryKeyGenerator(memberId, alarm.getAlarmType(), alarm.getCreatedAt())
                .toCompleteKeyWhichSpecifyOnlyOneValue();

        Map<String, List<SseEmitter>> memberEmittersMap = emitterRepository.findAllEmittersStartWithByMemberId(
                String.valueOf(memberId)
        );

        if (memberEmittersMap == null || memberEmittersMap.isEmpty()) {
            log.warn("No emitters found for member: {}", memberId);
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        // 모든 emitter 목록 반복
        memberEmittersMap.forEach((emitterKey, emitterList) -> {
            for (SseEmitter emitter : emitterList) {
                try {
                    emitter.send(SseEmitter.event()
                            .id(key)
                            .name(alarm.getAlarmType().getValue())
                            .data(alarm));
                    log.info("Sent SSE to member: {} with emitterKey: {}", memberId, emitterKey);
                } catch (IOException e) {
                    log.error("Failed to send SSE to member: {} with emitterKey: {}", memberId, emitterKey, e);
                    deadEmitters.add(emitter);
                }
            }

            // 실패한 emitter 삭제
            if (!deadEmitters.isEmpty()) {
                emitterList.removeAll(deadEmitters);
                log.info("Removed {} dead emitters for key: {}", deadEmitters.size(), emitterKey);
            }
        });
    }

    private String getEventId(Long memberId, LocalDateTime now, AlarmType eventName) {
        return memberId + "_" + eventName.getValue() + "_" + now;
    }

    public void sendNotification(String redisKey, Alarm alarm) {
        // 1. 전체 유저의 Emitter 가져오기
        List<SseEmitter> allEmitters = emitterRepository.getListByKeyPrefix("null_notification");

        // 2. 전체 전송
        for (SseEmitter emitter : allEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .id(getEventId(null, alarm.getCreatedAt(), AlarmType.NOTIFICATION))
                        .name(alarm.getAlarmType().getValue())
                        .data(alarm.getTitle()));
            } catch (IOException e) {
                log.warn("Failed to send broadcast alarm to key: {}", redisKey, e);
            }
        }
    }
}
