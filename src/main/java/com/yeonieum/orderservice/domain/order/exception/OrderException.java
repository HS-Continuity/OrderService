package com.yeonieum.orderservice.domain.order.exception;

import com.yeonieum.orderservice.global.exceptions.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OrderException extends CustomException {

    public OrderException(OrderExceptionCode orderExceptionCode, HttpStatus status) {
        super(orderExceptionCode, status);
    }
}

