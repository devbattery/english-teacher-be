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

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String AUTH_CODE_PREFIX = "auth_code:";

    // 임시 코드는 매우 짧은 시간 동안만 유효해야 됨
    private static final Duration AUTH_CODE_EXPIRATION = Duration.ofMinutes(1);

    /**
     * 토큰들을 저장하고 이에 대한 임시 인증 코드를 생성합니다.
     * @param tokens 저장할 AuthTokens 객체 (accessToken, refreshToken)
     * @return 생성된 임시 인증 코드
     */
    public String generateAndStoreTokens(AuthTokens tokens) {
        String code = UUID.randomUUID().toString();
        String key = AUTH_CODE_PREFIX + code;
        redisTemplate.opsForValue().set(key, tokens, AUTH_CODE_EXPIRATION);
        return code;
    }

    /**
     * 임시 인증 코드를 사용하여 저장된 토큰을 조회하고, 코드는 즉시 삭제합니다. (일회성)
     * @param code 프론트엔드에서 전달받은 임시 인증 코드
     * @return 저장되어 있던 AuthTokens 객체
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
