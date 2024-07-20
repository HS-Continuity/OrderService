package com.yeonieum.orderservice.global.responses.code;


import lombok.Getter;

@Getter
public enum SuccessCode {

    SELECT_SUCCESS(200, "200", "SELECT SUCCESS"),
    DELETE_SUCCESS(204, "204", "DELETE SUCCESS"),
    INSERT_SUCCESS(201, "201", "INSERT SUCCESS"),
    UPDATE_SUCCESS(200, "200", "UPDATE SUCCESS");

    private final int status;

    private final String code;

    private final String message;


    SuccessCode(final int status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
