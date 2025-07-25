package com.devbattery.englishteacher.common.exception;

public class UserUnauthorizedException extends CustomException {

    public UserUnauthorizedException() {
        super(ErrorCode.USER_UNAUTHORIZED);
    }

}
