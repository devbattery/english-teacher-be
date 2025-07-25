package com.devbattery.englishteacher.common.exception;

public class GeminiApiException extends CustomException {

    public GeminiApiException() {
        super(ErrorCode.GEMINI_API_ERROR);
    }

}
