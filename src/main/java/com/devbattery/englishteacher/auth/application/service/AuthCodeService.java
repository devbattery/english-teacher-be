package com.devbattery.englishteacher.auth.application.service;

import com.devbattery.englishteacher.auth.presentation.dto.AuthTokens;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCodeService {

    private static final String AUTH_CODE_PREFIX = "auth_code:";
    private static final Duration AUTH_CODE_EXPIRATION = Duration.ofMinutes(1);

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 토큰들을 저장하고 이에 대한 임시 인증 코드를 생성
     */
    public String generateAndStoreTokens(AuthTokens tokens) {
        String code = UUID.randomUUID().toString();
        String key = AUTH_CODE_PREFIX + code;
        redisTemplate.opsForValue().set(key, tokens, AUTH_CODE_EXPIRATION);
        return code;
    }

    /**
     * 임시 인증 코드를 사용하여 저장된 토큰을 조회하고, 코드는 즉시 삭제
     */
    public Optional<AuthTokens> retrieveAndRemoveTokens(String code) {
        String key = AUTH_CODE_PREFIX + code;
        AuthTokens tokens = (AuthTokens) redisTemplate.opsForValue().get(key);
        if (tokens != null) {
            redisTemplate.delete(key); // 한 번 사용된 코드는 즉시 삭제
        }
        return Optional.ofNullable(tokens);
    }

}
