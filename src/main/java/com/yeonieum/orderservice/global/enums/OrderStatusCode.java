package com.yeonieum.orderservice.global.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문대기 - 주문취소/결제완료 - 상품준비중 - 출고대기 - 배송시작(== 출고완료) - 배송중 - 배송완료 - 환불
 */
@Getter
@NoArgsConstructor
public enum OrderStatusCode {
    PENDING("PENDING"),
    CANCELED("CANCELED"),
    PAYMENT_COMPLETED("PAYMENT_COMPLETED"),
    PREPARING_PRODUCT("PREPARING_PRODUCT"),
    AWAITING_RELEASE("AWAITING_RELEASE"),
    SHIPPED("SHIPPED"),
    IN_DELIVERY("IN_DELIVERY"),
    DELIVERED("DELIVERED"),
    REFUND_REQUEST("REFUND_REQUEST"),
    REFUNDED("REFUNDED");

    private String code;

    OrderStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderStatusCode fromCode(String code) {
        switch (code) {
            case "PENDING":
                return PENDING;
            case "CANCELED":
                return CANCELED;
            case "PAYMENT_COMPLETED":
                return PAYMENT_COMPLETED;
            case "PREPARING_PRODUCT":
                return PREPARING_PRODUCT;
            case "AWAITING_RELEASE":
                return AWAITING_RELEASE;
            case "SHIPPED":
                return SHIPPED;
            case "IN_DELIVERY":
                return IN_DELIVERY;
            case "DELIVERED":
                return DELIVERED;
            case "REFUND_REQUEST":
                return REFUND_REQUEST;
            case "REFUNDED":
                return REFUNDED;
            default:
                throw new IllegalArgumentException("Invalid order status code: " + code);
        }
    }
}
