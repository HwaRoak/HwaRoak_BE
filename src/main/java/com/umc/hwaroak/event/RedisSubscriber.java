package com.umc.hwaroak.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.EmitterService;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private final EmitterService emitterService;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    private final StringRedisSerializer stringSerializer = new StringRedisSerializer();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = stringSerializer.deserialize(pattern);
        String redisKey = stringSerializer.deserialize(message.getBody());

        log.info("Received message on channel: [{}] with redisKey: {}", channel, redisKey);

        if (redisKey == null || redisKey.isBlank()) {
            log.warn("Empty message body received from channel: {}", channel);
            return;
        } else if (redisKey.startsWith("null_notification")) {
            handleBroadcast(redisKey);
        } else {
            String cleanedKey = redisKey.replace("\"", "");
            log.debug("Cleaned Redis Key: {}", cleanedKey);

            processMessage(cleanedKey, 5, Duration.ofMillis(100)); // max retry 5회
        }
    }

    private void processMessage(String redisKey, int maxRetries, Duration delay) {
        scheduledExecutorService.submit(() -> {
            try {
                String alarmJson = tryGetFromRedis(redisKey, maxRetries, delay);
                if (alarmJson != null) {
                    Alarm alarm = objectMapper.readValue(alarmJson, Alarm.class);
                    emitterService.send(alarm.getReceiver().getId(), alarm);
                    log.info("Successfully sent alarm [{}] to member [{}]", alarm.getId(), alarm.getReceiver().getId());
                } else {
                    log.warn("No alarm found in Redis for key: {}", redisKey);
                }
            } catch (Exception e) {
                log.error("Error processing Redis key: {}", redisKey, e);
                throw new GeneralException(ErrorCode.SSE_KEY_ERROR);
            }
        });
    }

    private String tryGetFromRedis(String key, int maxRetries, Duration delay) throws InterruptedException {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            String result = (String) redisTemplate.opsForValue().get(key);
            if (result != null) return result;

            log.debug("Retry [{}] for Redis key: {}", attempt, key);
            Thread.sleep(delay.toMillis());
        }
        return null;
    }

    private void handleBroadcast(String redisKey) {

        scheduledExecutorService.submit(() -> {
            try {
                String json = (String) redisTemplate.opsForValue().get(redisKey);
                if (json == null) {
                    log.warn("Broadcast alarm not found for key: {}", redisKey);
                    return;
                }
                Alarm alarm = objectMapper.readValue(json, Alarm.class);
                emitterService.sendNotification(redisKey, alarm);

            } catch (Exception e) {
                log.error("Error handling broadcast alarm", e);
            }
        });
    }
}
