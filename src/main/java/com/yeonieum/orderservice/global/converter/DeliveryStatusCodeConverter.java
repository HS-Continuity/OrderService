package com.yeonieum.orderservice.global.converter;

import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeliveryStatusCodeConverter implements AttributeConverter<DeliveryStatusCode, String> {

    @Override
    public String convertToDatabaseColumn(DeliveryStatusCode status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public DeliveryStatusCode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return DeliveryStatusCode.fromCode(dbData);
    }
}
