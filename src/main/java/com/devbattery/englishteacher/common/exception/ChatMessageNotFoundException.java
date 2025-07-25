package com.devbattery.englishteacher.common.exception;

public class ChatMessageNotFoundException extends CustomException {

    public ChatMessageNotFoundException() {
        super(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
    }

}
