package com.yeonieum.orderservice.domain.release.exception;

import com.yeonieum.orderservice.global.exceptions.code.CustomExceptionCode;

public enum ReleaseExceptionCode implements CustomExceptionCode {

    RELEASE_NOT_FOUND(12000, "존재하지 않는 출고 ID 입니다."),
    RELEASE_STATUS_TRANSITION_RULE_VIOLATION(12001, "출고 상태 트랜지션 룰 위반."),
    INVALID_ACCESS(12002, "잘못된 접근입니다."),
    INVALID_RELEASE_STATUS_CODE(12003, "잘못된 출고 상태 코드입니다."),
    START_DELIVERY_DATE_NOT_PROVIDED(12004, "배송 시작일을 입력하지 않았습니다."),
    UNIFORM_ORDER_VIOLATION(12005, "선택한 상품들의 회원, 배송지, 배송일, 출고 상태가 같은지 확인해주세요."),
    DELIVERY_DATE_REQUIRED(12006, "배송시작일을 입력하지 않으셨습니다!");

    private final int code;
    private final String message;

    ReleaseExceptionCode(int code, String message) {
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
