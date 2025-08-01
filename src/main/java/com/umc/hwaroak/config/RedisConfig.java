package com.umc.hwaroak.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    // Redis Lettuce 기반 연결
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost,redisPort);

        return new LettuceConnectionFactory(config);
    }


    // RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(ObjectMapper objectMapper) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        redisTemplate.setKeySerializer(new StringRedisSerializer()); // Redis key
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)); // Json을 저장
        redisTemplate.setHashKeySerializer(new StringRedisSerializer()); // Redis Hash key
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        redisTemplate.setEnableTransactionSupport(true);

        return redisTemplate;
    }
}
