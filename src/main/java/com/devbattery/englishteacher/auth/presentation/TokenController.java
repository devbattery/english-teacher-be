package com.devbattery.englishteacher.auth.presentation;

import com.devbattery.englishteacher.auth.application.service.AuthCodeService;
import com.devbattery.englishteacher.auth.application.service.RefreshTokenService;
import com.devbattery.englishteacher.auth.presentation.dto.AuthToken;
import com.devbattery.englishteacher.auth.presentation.dto.AuthTokenResponse;
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

    @PostMapping("/api/auth/token")
    public ResponseEntity<?> exchangeCodeForToken(@RequestBody Map<String, String> payload,
                                                  HttpServletResponse response) {
        String code = payload.get("code");
        if (code == null) {
            return ResponseEntity.badRequest().body("인증 코드가 없습니다.");
        }

        Optional<AuthToken> tokenOptional = authCodeService.getToken(code);
        if (tokenOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("틀리거나 만료된 토큰입니다.");
        }
        AuthToken tokens = tokenOptional.get();

        int cookieMaxAge = (int) (refreshTokenExpireTime / 1000);
        CookieUtil.addCookie(response, "refresh_token", tokens.getRefreshToken(), cookieMaxAge);

        return ResponseEntity.ok(new AuthTokenResponse(tokens.getAccessToken(), tokens.getIsNewUser()));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = CookieUtil.getCookie(request, "refresh_token")
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 refresh token입니다.");
        }

        // Redis에 저장된 토큰과 맞는지 재확인
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String savedToken = refreshTokenService.getToken(email);
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token not found or mismatched");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> refreshTokenCookie = CookieUtil.getCookie(request, "refresh_token");

        if (refreshTokenCookie.isPresent()) {
            String refreshToken = refreshTokenCookie.get().getValue();
            if (jwtTokenProvider.validateToken(refreshToken)) {
                String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                refreshTokenService.deleteToken(email);
            }
        }

        CookieUtil.deleteCookie(response, "refresh_token");

        return ResponseEntity.ok().build();
    }

}
