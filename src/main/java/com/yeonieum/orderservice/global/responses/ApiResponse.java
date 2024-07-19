package com.yeonieum.orderservice.global.responses;

import com.yeonieum.orderservice.global.responses.code.code.SuccessCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {
    private T result;

    private String resultCode;

    private String resultMsg;

    @Builder
    public ApiResponse(T result, SuccessCode successCode) {
        this.result = result;
        this.resultCode = successCode.getCode();
        this.resultMsg = successCode.getMessage();
    }
}
