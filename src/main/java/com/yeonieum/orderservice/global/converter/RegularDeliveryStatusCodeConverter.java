package com.yeonieum.orderservice.global.converter;

import com.yeonieum.orderservice.global.enums.RegularDeliveryStatusCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RegularDeliveryStatusCodeConverter implements AttributeConverter<RegularDeliveryStatusCode, String> {

    @Override
    public String convertToDatabaseColumn(RegularDeliveryStatusCode status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public RegularDeliveryStatusCode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return RegularDeliveryStatusCode.fromCode(dbData);
    }
}

