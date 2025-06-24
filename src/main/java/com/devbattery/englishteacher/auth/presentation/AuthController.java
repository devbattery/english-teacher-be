package com.devbattery.englishteacher.auth.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.user.application.service.UserReadService;
import com.devbattery.englishteacher.user.domain.entity.User;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserReadService userReadService;

    @GetMapping("/api/users/me")
    // ★★★ @AuthenticationPrincipal 대신 Authentication 객체를 직접 받습니다. ★★★
    public ResponseEntity<?> fetchCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // UserPrincipal 객체에서 필요한 정보를 일관된 방식으로 추출합니다.
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put("id", userPrincipal.getId());
        userAttributes.put("email", userPrincipal.getEmail());
        userAttributes.put("authorities", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // 소셜 로그인 시 받았던 추가 정보가 필요하다면?
        User user = userReadService.fetchByEmail(userPrincipal.getEmail());
        userAttributes.put("name", user.getName());
        userAttributes.put("picture", user.getImageUrl());

        return ResponseEntity.ok(userAttributes);
    }

}