package com.yeonieum.orderservice.global.converter;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReleaseStatusCodeConverter implements AttributeConverter<ReleaseStatusCode, String> {

    @Override
    public String convertToDatabaseColumn(ReleaseStatusCode status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public ReleaseStatusCode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ReleaseStatusCode.fromCode(dbData);
    }
}

