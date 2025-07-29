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

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("handleCustomException: {}", e.getErrorCode());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception in GlobalExceptionHandler: ", e);
        ErrorResponse response = new ErrorResponse(ErrorCode.SERVER_ERROR);
        return new ResponseEntity<>(response, ErrorCode.SERVER_ERROR.getStatus());
    }

}
