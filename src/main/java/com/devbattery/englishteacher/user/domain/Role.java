package com.devbattery.englishteacher.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "유저");

    private final String key;
    private final String description;

}
