package com.umc.hwaroak.infrastructure.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 공통 Publisher interface(알림, 공지 등)
 */
@Component
@Slf4j
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String channel, Object message) {
        log.info("Publishing message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
        redisTemplate.convertAndSend(channel, message);
        log.info("Published message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
    }
}