package com.yeonieum.orderservice.global.exceptions.handler;

import com.yeonieum.orderservice.global.exceptions.exception.CustomException;
import com.yeonieum.orderservice.global.responses.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Custom Exception Handler
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .exception(e)
                .build(), e.getStatus());
    }
}
