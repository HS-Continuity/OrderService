package com.yeonieum.orderservice.global.responses;

import com.yeonieum.orderservice.global.exceptions.exception.CustomException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private Map<String, String> errors;
    private int exceptionCode;
    private String exceptionMessage;
    private HttpStatus status;

    @Builder
    protected ErrorResponse(CustomException exception) {
        this.timestamp = LocalDateTime.now();
        this.exceptionMessage = exception.getCustomCode().getMessage();
        this.exceptionCode = exception.getCustomCode().getCode();
        this.status = exception.getStatus();
    }

    @Builder
    protected ErrorResponse(Map<String, String> errors, HttpStatus status) {
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
        this.status = status;
    }

    public static ErrorResponse of(Map<String, String> errors, HttpStatus status) {
        return new ErrorResponse(errors, status);
    }
}
