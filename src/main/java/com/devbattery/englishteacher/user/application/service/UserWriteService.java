package com.devbattery.englishteacher.user.application.service;

import com.devbattery.englishteacher.user.domain.entity.User;
import com.devbattery.englishteacher.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserWriteService {

    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public void update(User user) {
        userRepository.update(user);
    }

}
