package com.devbattery.englishteacher.common.exception;

public class JsonSerializedException extends CustomException {

    public JsonSerializedException() {
        super(ErrorCode.JSON_SERIALIZED_ERROR);
    }

}
