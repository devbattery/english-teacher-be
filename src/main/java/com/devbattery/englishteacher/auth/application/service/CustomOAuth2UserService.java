package com.devbattery.englishteacher.auth.application.service;

import com.devbattery.englishteacher.auth.domain.OAuth2Attributes;
import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.user.application.UserConvertor;
import com.devbattery.englishteacher.user.application.service.UserReadService;
import com.devbattery.englishteacher.user.application.service.UserWriteService;
import com.devbattery.englishteacher.user.domain.entity.User;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserReadService userReadService;
    private final UserWriteService userWriteService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> service = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = service.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());
        User user = login(attributes);

        // DefaultOAuth2User 대신 UserPrincipal을 생성하여 반환
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                oAuth2User.getAttributes()
        );
    }

    private User login(OAuth2Attributes attributes) {
        User user;
        try {
            user = userReadService.fetchByEmail(attributes.getEmail());
            userWriteService.update(user);
            log.info("{} 유저 정보 업데이트 완료.", attributes.getName());
        } catch (UsernameNotFoundException e) {
            user = UserConvertor.attributesToUser(attributes);
            userWriteService.save(user);
            log.info("{} 유저 정보 저장 완료.", attributes.getName());
        }

        return user;
    }

}
