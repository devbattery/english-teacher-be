package com.devbattery.englishteacher.user.infra.persistence.mybatis;

import static org.assertj.core.api.Assertions.assertThat;

import com.devbattery.englishteacher.user.domain.Role;
import com.devbattery.englishteacher.user.domain.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

@MybatisTest // https://mybatis.org/spring-boot-starter/mybatis-spring-boot-test-autoconfigure/
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @DisplayName("DB단으로 User가 생성되고 조회될 수 있다.")
    @Test
    void saveAndFindByEmail() {
        // given
        User user = new User("테스터", "test@mapper.com", "mapper.jpg", Role.USER);

        // when
        userMapper.save(user);
        Optional<User> foundUserOpt = userMapper.findByEmail("test@mapper.com");

        // then
        assertThat(foundUserOpt).isPresent();

        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getName()).isEqualTo("테스터");
        assertThat(foundUser.getEmail()).isEqualTo("test@mapper.com");
        assertThat(foundUser.getRoleKey()).isEqualTo("ROLE_USER");
    }

}