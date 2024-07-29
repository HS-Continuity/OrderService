package com.yeonieum.orderservice.global.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum RegularDeliveryStatusCode {
    PENDING("PENDING"),
    POSTPONE("POSTPONE"),
    COMPLETED("COMPLETED"),
    CANCELED("CANCELED");

    private String code;

    RegularDeliveryStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static RegularDeliveryStatusCode fromCode(String code) {
        switch (code) {
            case "PENDING":
                return PENDING;
            case "POSTPONE":
                return POSTPONE;
            case "COMPLETED":
                return COMPLETED;
            case "CANCELED":
                return CANCELED;
            default:
                throw new IllegalArgumentException("Invalid regular delivery reservation status code: " + code);
        }
    }
}
