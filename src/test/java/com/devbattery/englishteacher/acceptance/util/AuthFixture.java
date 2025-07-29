package com.devbattery.englishteacher.acceptance.util;

import static com.devbattery.englishteacher.acceptance.util.UserFixture.비회원;
import static com.devbattery.englishteacher.acceptance.util.UserFixture.회원_원준;

import java.util.Map;

public class AuthFixture {

    public static Map<String, Object> 비회원_로그인_요청() {
        return Map.of(
                "email", 비회원.getEmail(),
                "name", 비회원.getName(),
                "imageUrl", 비회원.getImageUrl());
    }

    public static Map<String, Object> 사용자_원준_로그인_요청() {
        return Map.of(
                "email", 회원_원준.getEmail(),
                "name", 회원_원준.getName(),
                "imageUrl", 회원_원준.getImageUrl());
    }

}
