package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.event.RedisPublisher;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.EmitterRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.EmitterService;
import com.umc.hwaroak.util.SseRepositoryKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final RedisPublisher redisPublisher;

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
            redisPublisher.publish("subscribe", "subscribe");
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

    private String getEventId(Long memberId, LocalDateTime now, AlarmType eventName) {
        return memberId + "_" + eventName.getValue() + "_" + now;
    }

    @Async
    @Override
    public void send(AlarmResponseDto.PreviewDto responseDto) {

        Long receiverId = (responseDto.getReceiverId() != null)
                ? responseDto.getReceiverId() : null;

        if (receiverId == null) {
            emitters.forEach((memberId, emitterList) -> {
                sendToEmitters(emitterList, responseDto);
            });
        } else {
            Map<String, List<SseEmitter>> emitterMap = emitterRepository.findAllEmittersStartWithByMemberId(
                    String.valueOf(receiverId)
            );

            if (emitterMap.isEmpty()) {
                log.warn("[{}]에 대한 연결 발견 불가능. 알림 미전송: {}", receiverId, responseDto);
                return;
            }

            emitterMap.forEach((key, emitterList) -> {
                log.info("[{}]에 대한 Emitter {}개에게 알림 전송 중...", receiverId, emitterList.size());
                sendToEmitters(emitterList, responseDto);
            });
        }
    }

    private void sendToEmitters(List<SseEmitter> emitterList, AlarmResponseDto.PreviewDto responseDto) {

        if (emitterList == null || emitterList.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitterList) {
            try {
                emitter.send(SseEmitter.event()
                        .name(responseDto.getAlarmType().getValue())
                        .data(responseDto)
                );
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitterList.removeAll(deadEmitters);
    }
}
