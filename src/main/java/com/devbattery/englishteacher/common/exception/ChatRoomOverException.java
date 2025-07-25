package com.devbattery.englishteacher.common.exception;

public class ChatRoomOverException extends CustomException {

    public ChatRoomOverException() {
        super(ErrorCode.CHAT_ROOM_OVER);
    }

}
