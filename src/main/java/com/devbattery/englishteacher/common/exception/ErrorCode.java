package com.devbattery.englishteacher.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    // 400 BAD_REQUEST
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력 값이 올바르지 않습니다."),

    // 500 INTERNAL_SERVER_ERROR
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}
