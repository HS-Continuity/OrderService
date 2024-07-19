package com.yeonieum.orderservice.global.enums;

public enum ReleaseStatusCode {
    AWAITING_RELEASE("AWAITING_RELEASE"),
    RELEASE_COMPLETED("RELEASE_COMPLETED");

    private final String code;

    ReleaseStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReleaseStatusCode fromCode(String code) {
        switch (code) {
            case "AWAITING_RELEASE":
                return AWAITING_RELEASE;
            case "RELEASE_COMPLETED":
                return RELEASE_COMPLETED;
            default:
                throw new IllegalArgumentException("Invalid release status code: " + code);
        }
    }
}
