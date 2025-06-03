package com.devbattery.englishteacher.common.handler;

import com.devbattery.englishteacher.auth.application.service.RefreshTokenService;
import com.devbattery.englishteacher.common.util.CookieUtil;
import com.devbattery.englishteacher.common.util.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공. 토큰 생성 및 처리 시작.");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email"); // loadUser()의 return

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        log.info("Access Token 생성 완료.");

        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        refreshTokenService.saveToken(email, refreshToken);
        log.info("Redis에 Refresh Token 생성 및 저장 완료.");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // HttpOnly 쿠키 전송
        int cookieMaxAge = (int) (refreshTokenExpireTime / 1000);
        CookieUtil.addCookie(response, "refresh_token", refreshToken, cookieMaxAge);

        // 본문 JSON 전송
        Map<String, String> tokenMap = Map.of("accessToken", accessToken);
        response.getWriter().write(objectMapper.writeValueAsString(tokenMap));

        log.info("클라이언트로 토큰 응답 완료.");
    }

}
