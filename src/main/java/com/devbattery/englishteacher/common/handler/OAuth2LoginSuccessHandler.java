package com.devbattery.englishteacher.common.handler;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.auth.application.service.AuthCodeService;
import com.devbattery.englishteacher.auth.application.service.RefreshTokenService;
import com.devbattery.englishteacher.auth.presentation.dto.AuthTokens;
import com.devbattery.englishteacher.common.util.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthCodeService authCodeService;

    @Value("${url.base}")
    private String url;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공. 임시 인증 코드 생성 시작.");

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getEmail();
        boolean isNewUser = userPrincipal.isNewUser();

        // 1. Access/Refresh Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // 2. Refresh Token은 Redis에 저장 (기존 로직 유지)
        refreshTokenService.saveToken(email, refreshToken);
        log.info("Redis에 Refresh Token 저장 완료.");

        // 3. Access/Refresh Token을 임시 저장하고 일회용 인증 코드 생성
        AuthTokens authTokens = new AuthTokens(accessToken, refreshToken, isNewUser);
        String authorizationCode = authCodeService.generateAndStoreTokens(authTokens);
        log.info("임시 인증 코드 생성 완료: {}", authorizationCode);

        // 4. 프론트엔드로 임시 인증 코드를 담아 리다이렉트
        String redirectUrl = determineTargetUrl(request, response, authorizationCode);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", redirectUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        log.info("클라이언트로 임시 인증 코드를 담아 리다이렉트 완료.");
    }

    // `determineTargetUrl` 메서드 시그니처를 수정하여 accessToken 대신 code를 받도록 합니다.
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, String code) {
        String targetUrl = url + "/auth/callback"; // 프론트엔드의 콜백 페이지

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("code", code) // "token" 대신 "code" 사용
                .build().toUriString();
    }

    // 이 메서드는 더 이상 직접 사용되지 않으므로, 삭제하거나 기본 구현을 유지합니다.
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        // 이 메서드는 우리가 직접 호출하지 않으므로, 기본 URL을 반환하도록 둘 수 있습니다.
        return url;
    }

}