package com.yeonieum.orderservice.global.exceptions.exception;

import com.yeonieum.orderservice.global.exceptions.code.CustomExceptionCode;
import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{
    private final HttpStatus status;
    private final CustomExceptionCode customExceptionCode;

    public CustomException(CustomExceptionCode customExceptionCode, HttpStatus status) {
        super(customExceptionCode.getMessage());
        this.status = status;
        this.customExceptionCode = customExceptionCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public CustomExceptionCode getCustomCode() {
        return customExceptionCode;
    }

    @Override
    public String getMessage() {
        return customExceptionCode.getMessage();
    }
}
