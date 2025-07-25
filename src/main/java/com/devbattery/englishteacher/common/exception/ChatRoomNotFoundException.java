package com.devbattery.englishteacher.common.exception;

public class ChatRoomNotFoundException extends CustomException {

    public ChatRoomNotFoundException() {
        super(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }

}
