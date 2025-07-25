package com.devbattery.englishteacher.user.application.service;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.auth.presentation.dto.CurrentUserResponse;
import com.devbattery.englishteacher.user.domain.entity.User;
import com.devbattery.englishteacher.user.domain.repository.UserRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReadService {

    private final UserRepository userRepository;

    public User fetchByEmail(String email) {
        return userRepository.fetchByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email)); // TODO: Custom Exception
    }

    public CurrentUserResponse fetchCurrentUserResponse(String email, UserPrincipal userPrincipal) {
        User user = fetchByEmail(email);
        return CurrentUserResponse.builder()
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .name(user.getName())
                .picture(user.getImageUrl())
                .authorities(userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    public User fetchById(Long userId) {
        return userRepository.fetchById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(String.valueOf(userId))); // TODO: Custom Exception
    }

}
