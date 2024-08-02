package com.yeonieum.orderservice.domain.release.exception;

import com.yeonieum.orderservice.global.exceptions.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ReleaseException extends CustomException {

    public ReleaseException(ReleaseExceptionCode releaseExceptionCode, HttpStatus status) {
        super(releaseExceptionCode, status);
    }
}
