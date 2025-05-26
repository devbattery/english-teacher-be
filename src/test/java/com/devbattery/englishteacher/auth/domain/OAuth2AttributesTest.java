package com.devbattery.englishteacher.auth.domain;

import static org.assertj.core.api.Assertions.*;

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

}