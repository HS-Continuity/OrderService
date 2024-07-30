package com.yeonieum.orderservice.global.exceptions.handler;

import com.yeonieum.orderservice.global.exceptions.exception.CustomException;
import com.yeonieum.orderservice.global.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Custom Exception Handler
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .exception(e)
                .build(), e.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String , String> errorMap = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(ErrorResponse.of(errorMap, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }
}

