package com.yeonieum.orderservice.domain.delivery.exception;

import com.yeonieum.orderservice.global.exceptions.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DeliveryException extends CustomException {

    public DeliveryException(DeliveryExceptionCode deliveryExceptionCode, HttpStatus status) {
        super(deliveryExceptionCode, status);
    }
}
