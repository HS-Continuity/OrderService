package com.yeonieum.orderservice.domain.order.exception;

import com.yeonieum.orderservice.global.exceptions.code.CustomExceptionCode;

public enum OrderExceptionCode implements CustomExceptionCode {

    ORDER_NOT_FOUND(11000, "존재하지 않는 주문 ID 입니다."),
    ORDER_STATUS_TRANSITION_RULE_VIOLATION(11001, "주문상태 트랜지션 룰 위반."),
    INVALID_ACCESS(11002, "잘못된 접근입니다."),
    PRODUCT_NOT_FOUND(11003, "존재하지 않는 상품입니다."),
    COUPON_ALREADY_USED(11004, "이미 사용한 쿠폰입니다."),
    COUPON_USE_FAILED(11005, "쿠폰 사용에 실패했습니다."),
    ORDER_ID_NOT_FOUND(11006, "하나 이상의 주문 ID가 존재하지 않습니다.");

    private final int code;
    private final String message;

    OrderExceptionCode(int code, String message) {
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
