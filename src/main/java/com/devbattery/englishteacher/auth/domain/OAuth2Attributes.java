package com.devbattery.englishteacher.auth.domain;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuth2Attributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String imageUrl;

    @Builder
    private OAuth2Attributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email,
                             String imageUrl) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public static OAuth2Attributes of(String registrationId, String userNameAttributeName,
                                      Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }

        if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }

        return ofGoogle(userNameAttributeName, attributes);
    }

    /**
     * Google 사용자 정보를 받아 OAuthAttributes 객체 생성
     */
    private static OAuth2Attributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .imageUrl((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * Naver 사용자 정보를 받아 OAuthAttributes 객체 생성
     * Naver의 응답값은 "response"라는 키 값 내부에 실제 사용자 정보가 들어있습니다.
     */
    private static OAuth2Attributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2Attributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .imageUrl((String) response.get("profile_image"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * Kakao 사용자 정보를 받아 OAuthAttributes 객체 생성
     * Kakao의 응답값은 "kakao_account" 키 내부에 이메일이,
     * "kakao_account" 내부의 "profile" 키 내부에 닉네임과 프로필 사진이 들어있습니다.
     */
    private static OAuth2Attributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        // "kakao_account"에 사용자 정보가 담겨 있음
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        // "kakao_account" 안의 "profile"에 닉네임, 프로필 사진이 담겨 있음
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2Attributes.builder()
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .imageUrl((String) kakaoProfile.get("profile_image_url"))
                .attributes(attributes) // 최상위 맵을 attributes로 사용
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

}
