package com.devbattery.englishteacher.user.domain.repository;

import com.devbattery.englishteacher.user.domain.entity.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> fetchByEmail(String email);

    void save(User user);

    void update(User user);

    Optional<User> fetchById(Long userId);

}
