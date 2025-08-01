package com.umc.hwaroak.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
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
