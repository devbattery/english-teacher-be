package com.devbattery.englishteacher.acceptance.util;

import java.util.Map;

public enum UserFixture {

    회원_원준("wj1234@gmail.com", "정원준", "https://www"), 비회원("asd1234@naver.com", "성이름", "https://www");

    private String email;
    private String name;
    private String imageUrl;

    UserFixture(String email, String name, String imageUrl) {
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public static Map<String, Object> 사용자_원준_회원가입_요청() {
        return Map.of("email", 회원_원준.email, "name", 회원_원준.name, "imageUrl", 회원_원준.imageUrl);
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
