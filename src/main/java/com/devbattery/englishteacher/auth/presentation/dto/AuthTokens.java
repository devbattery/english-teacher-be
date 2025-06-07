package com.devbattery.englishteacher.auth.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokens {

    private String accessToken;
    private String refreshToken;

}
