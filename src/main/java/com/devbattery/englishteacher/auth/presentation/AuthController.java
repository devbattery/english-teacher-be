package com.devbattery.englishteacher.auth.presentation;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/users/me")
    // ★★★ @AuthenticationPrincipal 대신 Authentication 객체를 직접 받습니다. ★★★
    public ResponseEntity<?> fetchCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        Map<String, Object> userAttributes;

        // Principal의 타입에 따라 분기 처리
        if (principal instanceof OAuth2User) {
            // 소셜 로그인 직후의 경우
            userAttributes = ((OAuth2User) principal).getAttributes();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            // 토큰 재발급 이후의 경우
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) principal;
            // User 객체에서는 기본적인 정보만 가져올 수 있습니다. (예: email)
            // 더 많은 정보가 필요하다면, DB에서 이메일로 사용자 정보를 조회해야 합니다.
            userAttributes = Map.of(
                    "email", user.getUsername(),
                    "authorities", user.getAuthorities()
            );
        } else {
            // 예상치 못한 Principal 타입
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unsupported principal type");
        }

        return ResponseEntity.ok(userAttributes);
    }

}