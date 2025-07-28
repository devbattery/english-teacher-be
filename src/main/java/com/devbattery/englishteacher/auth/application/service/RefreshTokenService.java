package com.devbattery.englishteacher.auth.application.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    public void saveToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                refreshTokenExpireTime,
                TimeUnit.MILLISECONDS
        );
    }

    public String getToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);
    }

}
