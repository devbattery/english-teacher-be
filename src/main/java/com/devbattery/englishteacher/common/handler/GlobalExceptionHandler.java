package com.devbattery.englishteacher.common.handler;

import com.devbattery.englishteacher.common.exception.CustomException;
import com.devbattery.englishteacher.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 직접 정의한 CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("handleCustomException: {}", e.getErrorCode());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(response, e.getErrorCode().getStatus());
    }

    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handleException: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(ErrorCode.SERVER_ERROR);
        return new ResponseEntity<>(response, ErrorCode.SERVER_ERROR.getStatus());
    }

}
