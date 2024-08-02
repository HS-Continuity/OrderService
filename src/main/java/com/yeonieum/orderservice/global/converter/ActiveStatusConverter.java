package com.yeonieum.orderservice.global.converter;

import com.yeonieum.orderservice.global.enums.ActiveStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActiveStatusConverter implements AttributeConverter<ActiveStatus, Character> {

    @Override
    public Character convertToDatabaseColumn(ActiveStatus status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public ActiveStatus convertToEntityAttribute(Character dbData) {
        if (dbData == null) {
            return null;
        }
        return ActiveStatus.fromCode(dbData);
    }
}
