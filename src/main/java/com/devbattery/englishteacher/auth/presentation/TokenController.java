package com.devbattery.englishteacher.auth.presentation;

import com.devbattery.englishteacher.auth.application.service.AuthCodeService;
import com.devbattery.englishteacher.auth.application.service.RefreshTokenService;
import com.devbattery.englishteacher.auth.presentation.dto.AuthTokens;
import com.devbattery.englishteacher.common.util.CookieUtil;
import com.devbattery.englishteacher.common.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthCodeService authCodeService;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    /**
     * OAuth2 로그인 성공 후 받은 임시 인증 코드를 실제 토큰으로 교환합니다.
     */
    @PostMapping("/api/auth/token")
    public ResponseEntity<?> exchangeCodeForTokens(@RequestBody Map<String, String> payload,
                                                   HttpServletResponse response) {
        String code = payload.get("code");
        if (code == null) {
            return ResponseEntity.badRequest().body("Authorization code is missing.");
        }

        Optional<AuthTokens> tokensOptional = authCodeService.retrieveAndRemoveTokens(code);

        if (tokensOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired authorization code.");
        }

        AuthTokens tokens = tokensOptional.get();

        // HttpOnly 쿠키에 Refresh Token 설정
        int cookieMaxAge = (int) (refreshTokenExpireTime / 1000);
        CookieUtil.addCookie(response, "refresh_token", tokens.getRefreshToken(), cookieMaxAge);

        // Access Token은 응답 바디로 전달
        return ResponseEntity.ok(Map.of("accessToken", tokens.getAccessToken()));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = CookieUtil.getCookie(request, "refresh_token")
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String savedToken = refreshTokenService.getToken(email);

        // Redis에 저장된 토큰과 맞는지 재확인
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token not found or mismatched");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    /**
     * 로그아웃을 처리합니다. 클라이언트의 Refresh Token 쿠키를 무효화하고, 서버(Redis)에서도 삭제합니다.
     */
    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 리프레시 토큰 가져오기
        Optional<Cookie> refreshTokenCookie = CookieUtil.getCookie(request, "refresh_token");

        if (refreshTokenCookie.isPresent()) {
            String refreshToken = refreshTokenCookie.get().getValue();
            // 2. Redis에서 해당 리프레시 토큰 삭제
            if (jwtTokenProvider.validateToken(refreshToken)) {
                String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                refreshTokenService.deleteToken(email);
            }
        }

        // 3. 클라이언트의 리프레시 토큰 쿠키를 삭제
        CookieUtil.addCookie(response, "refresh_token", "", 0); // maxAge를 0으로 설정하여 즉시 만료

        return ResponseEntity.ok("Successfully logged out");
    }

}
