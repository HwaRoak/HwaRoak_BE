package com.umc.hwaroak.config;

import com.umc.hwaroak.event.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final RedisSubscriber redisSubscriber;
    private final RedisConnectionFactory redisConnectionFactory;

    // Pub/Sub 메시지 처리 Listener
    @Bean
    public RedisMessageListenerContainer redisMessageListener(){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        container.addMessageListener(redisSubscriber, new ChannelTopic("notification"));

        return container;
    }
}
