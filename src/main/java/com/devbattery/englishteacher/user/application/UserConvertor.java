package com.devbattery.englishteacher.user.application;

import com.devbattery.englishteacher.auth.domain.OAuth2Attributes;
import com.devbattery.englishteacher.user.domain.Role;
import com.devbattery.englishteacher.user.domain.entity.User;

public class UserConvertor {

    private UserConvertor() {
    }

    public static User attributesToUser(OAuth2Attributes attributes) {
        return new User(attributes.getName(), attributes.getEmail(), attributes.getImageUrl(), Role.USER);
    }

}
