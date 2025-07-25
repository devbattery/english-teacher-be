package com.devbattery.englishteacher.common.exception;

public class TokenUnauthorizedException extends CustomException {

    public TokenUnauthorizedException() {
        super(ErrorCode.USER_UNAUTHORIZED);
    }

}
