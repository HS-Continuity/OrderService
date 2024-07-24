package com.yeonieum.orderservice.global.enums;

public enum ActiveStatus {
    ACTIVE('T'),
    INACTIVE('F');

    private final char code;

    ActiveStatus(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }

    public static ActiveStatus fromCode(char code) {
        switch (code) {
            case 'T':
                return ACTIVE;
            case 'F':
                return INACTIVE;
            default:
                throw new IllegalArgumentException("Invalid status code: " + code);
        }
    }
}

