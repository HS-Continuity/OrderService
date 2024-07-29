package com.yeonieum.orderservice.domain.delivery.exception;

import com.yeonieum.orderservice.global.exceptions.code.CustomExceptionCode;

public enum DeliveryExceptionCode implements CustomExceptionCode {

    DELIVERY_STATUS_NOT_FOUND(10000, "존재하지 않는 배송상태 코드 입니다.");

    private final int code;
    private final String message;

    DeliveryExceptionCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
