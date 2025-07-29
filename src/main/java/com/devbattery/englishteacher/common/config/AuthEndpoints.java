package com.devbattery.englishteacher.common.config;

public class AuthEndpoints {

    private AuthEndpoints() {
    }

    public static final String[] PERMIT_ALL_PATTERNS = {
            // 정적 리소스
            "/",
            "/css/**",
            "/images/**",
            "/uploads/**",
            "/js/**",
            "/h2-console/**",
            "/docs/**",
            // 소셜 로그인 관련
            "/oauth2/**",
            // 토큰 관련 API
            "/api/auth/token",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/test/login"
    };

}
