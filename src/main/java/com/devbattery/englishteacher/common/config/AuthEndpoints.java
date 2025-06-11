package com.devbattery.englishteacher.common.config;

public class AuthEndpoints {

    private AuthEndpoints() {
    }

    /**
     * Spring Security 및 JWT 필터에서 인증을 요구하지 않을 엔드포인트 목록입니다.
     */
    public static final String[] PERMIT_ALL_PATTERNS = {
            // 정적 리소스
            "/",
            "/css/**",
            "/images/**",
            "/js/**",
            "/h2-console/**",
            // 소셜 로그인 관련
            "/oauth2/**",
            // 토큰 관련 API
            "/api/auth/token",
            "/api/auth/refresh",
            "/api/auth/logout"
    };

}
