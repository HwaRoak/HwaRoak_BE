package com.umc.hwaroak.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.util.SseRepositoryKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String channel, Object message) {
        log.info("Publishing message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
        redisTemplate.convertAndSend(channel, message);
        log.info("Published message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
    }

    public void saveAlarmWithTTL(String key, Alarm alarm, long ttl, TimeUnit timeUnit) {
        try {
            String alarmToJson = objectMapper.writeValueAsString(alarm);
            redisTemplate.opsForValue().set(key, alarmToJson, ttl, timeUnit);
            log.debug("Saved notification with key: {} and TTL: {} {}", key, ttl, timeUnit);
        } catch (Exception e) {
            log.error("Error saving notification with key: {} and TTL: {} {}", key, ttl, timeUnit, e);
        }
    }

    public void publishGlobalAlarm(Alarm alarm, long ttl, TimeUnit timeUnit) {

        redisTemplate.convertAndSend("notification", alarm);
        String redisKey = new SseRepositoryKeyGenerator(null, alarm.getAlarmType(), alarm.getCreatedAt())
                .toCompleteKeyWhichSpecifyOnlyOneValue();

        try {
            String alarmToJson = objectMapper.writeValueAsString(alarm);
            redisTemplate.opsForValue().set(redisKey, alarmToJson, ttl, timeUnit);
            log.debug("Saved notification with key: {} and TTL: {} {}", redisKey, ttl, timeUnit);
            publish("notification", alarm);
        } catch (Exception e) {
            log.error("Error saving notification with key: {} and TTL: {} {}", redisKey, ttl, timeUnit, e);
        }
    }
}
