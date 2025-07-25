package com.devbattery.englishteacher.common.handler;

import com.devbattery.englishteacher.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

}
