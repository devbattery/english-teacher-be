package com.devbattery.englishteacher.user.infra.persistence.mybatis;

import com.devbattery.englishteacher.user.domain.entity.User;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    Optional<User> findByEmail(String email);

    void save(User user);

    void update(User user);

}
