package com.yeonieum.orderservice.global.enums;

public enum DeliveryStatusCode {
    SHIPPED("SHIPPED"),
    IN_DELIVERY("IN_DELIVERY"),
    DELIVERED("DELIVERED");

    private final String code;

    DeliveryStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DeliveryStatusCode fromCode(String code) {
        switch (code) {
            case "SHIPPED":
                return SHIPPED;
            case "IN_DELIVERY":
                return IN_DELIVERY;
            case "DELIVERED":
                return DELIVERED;
            default:
                throw new IllegalArgumentException("Invalid delivery status code: " + code);
        }
    }
}
