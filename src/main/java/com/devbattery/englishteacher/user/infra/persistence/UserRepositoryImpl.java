package com.devbattery.englishteacher.user.infra.persistence;

import com.devbattery.englishteacher.user.domain.entity.User;
import com.devbattery.englishteacher.user.domain.repository.UserRepository;
import com.devbattery.englishteacher.user.infra.persistence.mybatis.UserMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public Optional<User> fetchByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public void save(User user) {
        userMapper.save(user);
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public Optional<User> fetchById(Long userId) {
        return userMapper.findById(userId);
    }

}
