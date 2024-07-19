package com.yeonieum.orderservice.global.enums;

public enum DayOfWeek {
    SUNDAY("SUNDAY", 7),
    MONDAY("MONDAY", 1),
    TUESDAY("TUESDAY", 2),
    WEDNESDAY("WEDNESDAY", 3),
    THURSDAY("THURSDAY", 4),
    FRIDAY("FRIDAY", 5),
    SATURDAY("SATURDAY", 6);

    private final String code;
    private final int value;

    DayOfWeek(String code, int value) {
        this.code = code;
        this.value = value;
    }

    public String getStoredDayValue() {
        return code;
    }

    public int getValue() {
        return value;
    }



    public static DayOfWeek fromCode(String code) {
        switch (code) {
            case "SUNDAY":
                return SUNDAY;
            case "MONDAY":
                return MONDAY;
            case "TUESDAY":
                return TUESDAY;
            case "WEDNESDAY":
                return WEDNESDAY;
            case "THURSDAY":
                return THURSDAY;
            case "FRIDAY":
                return FRIDAY;
            case "SATURDAY":
                return SATURDAY;
            default:
                throw new IllegalArgumentException("Invalid day of week code: " + code);
        }
    }
}
