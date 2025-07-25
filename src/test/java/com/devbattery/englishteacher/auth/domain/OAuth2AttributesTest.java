package com.devbattery.englishteacher.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuth2AttributesTest {

    @DisplayName("구글 로그인 시, 사용자 정보를 정확히 파싱한다.")
    @Test
    void ofGoogle() {
        // given
        String registrationId = "google";
        String userNameAttributeName = "sub";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "구글");
        attributes.put("email", "test@gmail.com");
        attributes.put("picture", "google.jpg");

        // when
        OAuth2Attributes result = OAuth2Attributes.of(registrationId, userNameAttributeName, attributes);

        // then
        assertThat(result.getName()).isEqualTo("구글");
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getImageUrl()).isEqualTo("google.jpg");

        assertThat(result.getNameAttributeKey()).isEqualTo(userNameAttributeName);
    }

    @Test
    @DisplayName("네이버 로그인 시 사용자 정보를 정확히 파싱한다")
    void ofNaver() {
        // given
        String registrationId = "naver";
        String userNameAttributeName = "id"; // 네이버는 'response' 안에 'id'가 키
        Map<String, Object> response = new HashMap<>();
        response.put("name", "네이버");
        response.put("email", "test@naver.com");
        response.put("profile_image", "naver.jpg");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", response); // 네이버는 response 키 안에 정보가 있음

        // when
        // ofNaver는 attributes.get("response")를 내부적으로 처리하도록 수정해야 함
        OAuth2Attributes result = OAuth2Attributes.of(registrationId, userNameAttributeName, attributes);

        // then
        assertThat(result.getName()).isEqualTo("네이버");
        assertThat(result.getEmail()).isEqualTo("test@naver.com");
        assertThat(result.getImageUrl()).isEqualTo("naver.jpg");
    }

    @Test
    @DisplayName("카카오 로그인 시 사용자 정보를 정확히 파싱한다")
    void ofKakao() {
        // given
        String registrationId = "kakao";
        String userNameAttributeName = "id";
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "test@kakao.com");

        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오");
        profile.put("profile_image_url", "kakao.jpg");
        kakaoAccount.put("profile", profile);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("kakao_account", kakaoAccount);

        // when
        OAuth2Attributes result = OAuth2Attributes.of(registrationId, userNameAttributeName, attributes);

        // then
        assertThat(result.getName()).isEqualTo("카카오");
        assertThat(result.getEmail()).isEqualTo("test@kakao.com");
        assertThat(result.getImageUrl()).isEqualTo("kakao.jpg");
    }

}