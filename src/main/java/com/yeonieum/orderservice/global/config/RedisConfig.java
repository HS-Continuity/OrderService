package com.yeonieum.orderservice.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword("your-password");
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisOperations<String, Long> orderEventRedisOperations(RedisConnectionFactory redisConnectionFactory) {
        final Jackson2JsonRedisSerializer<Long> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Long.class);
        final RedisTemplate<String, Long> orderEventRedisTemplate = new RedisTemplate<>();

        orderEventRedisTemplate.setConnectionFactory(redisConnectionFactory);
        orderEventRedisTemplate.setKeySerializer(RedisSerializer.string());
        orderEventRedisTemplate.setValueSerializer(jsonRedisSerializer);
        orderEventRedisTemplate.setHashKeySerializer(RedisSerializer.string());
        orderEventRedisTemplate.setHashValueSerializer(jsonRedisSerializer);
        return orderEventRedisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        final RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        return redisMessageListenerContainer;
    }
}
