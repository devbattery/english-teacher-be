package com.devbattery.englishteacher.common.handler;

import com.devbattery.englishteacher.auth.application.service.AuthCodeService;
import com.devbattery.englishteacher.auth.application.service.RefreshTokenService;
import com.devbattery.englishteacher.auth.domain.UserPrincipal;
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

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveToken(email, refreshToken);
        log.info("Redis에 Refresh Token 저장 완료.");

        AuthTokens authTokens = new AuthTokens(accessToken, refreshToken, isNewUser);
        String authorizationCode = authCodeService.generateAndStoreTokens(authTokens);
        log.info("임시 인증 코드 생성 완료: {}", authorizationCode);

        String redirectUrl = determineTargetUrl(request, response, authorizationCode);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", redirectUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        log.info("클라이언트로 임시 인증 코드를 담아 리다이렉트 완료.");
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, String code) {
        String targetUrl = url + "/auth/callback";

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("code", code)
                .build().toUriString();
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        return url;
    }

}