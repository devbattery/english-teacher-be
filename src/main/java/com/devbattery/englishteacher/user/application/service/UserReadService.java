package com.devbattery.englishteacher.user.application.service;

import com.devbattery.englishteacher.user.domain.entity.User;
import com.devbattery.englishteacher.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public User fetchById(Long userId) {
        return userRepository.fetchById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(String.valueOf(userId))); // TODO: Custom Exception
    }

}
