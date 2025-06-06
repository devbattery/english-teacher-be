package com.devbattery.englishteacher.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
        config.setPassword(redisProperties.getPassword());
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(config);
        log.info("LettuceConnectionFactory 생성 완료.");
        return lettuceConnectionFactory;
    }

    /**
     * 기존 redisTemplate<String, String>을 대체합니다.
     * 이제 이 RedisTemplate은 key로 String을, value로 Object를 처리할 수 있습니다.
     * 이를 통해 RefreshToken(String)과 AuthTokens(Object)를 모두 저장할 수 있습니다.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key Serializer는 String으로 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Value Serializer는 Jackson 라이브러리를 이용한 JSON 직렬화 방식으로 설정
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash Key Serializer도 String으로 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Hash Value Serializer도 JSON 직렬화 방식으로 설정
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

}