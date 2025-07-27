package com.devbattery.englishteacher.common.exception;

public class ContentGenerationFailedException extends CustomException {

    public ContentGenerationFailedException() {
        super(ErrorCode.CONTENT_GENERATION_FAILED);
    }

}
