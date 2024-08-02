package com.yeonieum.orderservice.global.converter;

import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusCodeConverter implements AttributeConverter<OrderStatusCode, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatusCode status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public OrderStatusCode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return OrderStatusCode.fromCode(dbData);
    }
}
