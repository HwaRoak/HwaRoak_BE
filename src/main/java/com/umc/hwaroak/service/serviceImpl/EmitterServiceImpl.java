package com.umc.hwaroak.service.serviceImpl;

import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.infrastructure.publisher.RedisPublisher;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.EmitterRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.EmitterService;
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
    private final RedisPublisher redisPublisher;

    @Override
    public SseEmitter subscribe(String lastEventId) {

        LocalDateTime now = LocalDateTime.now();
        Long memberId = memberLoader.getCurrentMemberId();

        SseEmitter sse = new SseEmitter(Long.MAX_VALUE);

        // Key(filtering) : memberId
        // 검사
        emitters.computeIfAbsent(String.valueOf(memberId), k -> new ArrayList<>())
                .add(sse);
        sse.onCompletion(() -> {
            log.info("onCompletion callback : {}", memberId);
            emitterRepository.remove(String.valueOf(memberId));
        });
        sse.onTimeout(() -> {
            log.info("onTimeout callback");
            sse.complete();
        });
        sse.onError((Throwable th) -> {
            emitterRepository.remove(String.valueOf(memberId));
            log.error("Emitter eorror for key: {} with {}",
                    String.valueOf(memberId), th.getMessage());
        });

        // 새로 등록 저장
        emitterRepository.put(String.valueOf(memberId), sse);
        try {
            redisPublisher.publish("subscribe", "subscribe");
            sse.send(SseEmitter.event()
                    .name(AlarmType.CONNECTED.getValue())
                    .id(getEventId(memberId, AlarmType.CONNECTED, now))
                    .data("subscribe")
            );
        } catch (IOException e) {
            emitterRepository.remove(String.valueOf(memberId));
            log.info("SSE Exception: {}", e.getMessage());
            throw new GeneralException(ErrorCode.SSE_CONNECTION_ERROR);
        }
        return sse;
    }

    /**
     * 고유한 SSE의 ID
     * @param memberId
     * @param eventName
     * @param now
     * @return
     */
    private String getEventId(Long memberId, AlarmType eventName, LocalDateTime now) {
        return memberId + "_" + eventName.getValue() + "_" + now;
    }

    /**
     * 기존의 SSE에 새로 전송
     * @param responseDto
     */
    @Override
    public void send(AlarmResponseDto.PreviewDto responseDto) {

        Long receiverId = (responseDto.getReceiverId() != null)
                ? responseDto.getReceiverId() : null;

        if (receiverId == null) { // 특정 receiver가 존재하지 않을 경우
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
