package com.devbattery.englishteacher.user.domain.entity;

import com.devbattery.englishteacher.user.domain.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

    private Long id;
    private String name;
    private String email;
    private String imageUrl;
    private Role role;

    public User(String name, String email, String imageUrl, Role role) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }

}
