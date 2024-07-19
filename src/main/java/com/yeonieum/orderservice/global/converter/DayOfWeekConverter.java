package com.yeonieum.orderservice.global.converter;

import com.yeonieum.orderservice.global.enums.DayOfWeek;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, String> {

    @Override
    public String convertToDatabaseColumn(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            return null;
        }
        return dayOfWeek.getStoredDayValue();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return DayOfWeek.fromCode(dbData);
    }
}


