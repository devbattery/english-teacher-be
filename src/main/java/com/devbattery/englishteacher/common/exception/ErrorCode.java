package com.devbattery.englishteacher.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    TOKEN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "토큰의 권한이 없습니다."),

    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "유저의 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅 메시지를 찾을 수 없습니다."),
    CHAT_ROOM_OVER(HttpStatus.BAD_REQUEST, "한 레벨당 채팅방은 10개까지만 생성됩니다."),

    CONTENT_GENERATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "콘텐츠 생성 대기 중 오류가 발생했습니다."),
    GEMINI_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "Gemini 호출에 실패했습니다."),

    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리에 실패했습니다"),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력 값이 올바르지 않습니다."),

    // 500 INTERNAL_SERVER_ERROR
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}
