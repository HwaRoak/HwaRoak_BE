package com.umc.hwaroak.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.service.EmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;

    private final EmitterService emitterService;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            AlarmResponseDto.PreviewDto responseDto = objectMapper.readValue(body, AlarmResponseDto.PreviewDto.class);
            log.info("Received alarm via Redis: {}", responseDto);

            emitterService.send(responseDto);
        } catch (Exception e) {
            log.error("Failed to handle Redis message", e, e.getMessage());
        }
    }
}
