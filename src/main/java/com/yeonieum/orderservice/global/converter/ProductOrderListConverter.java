package com.yeonieum.orderservice.global.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.domain.order.entity.ProductOrderListEntity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;

@Converter
public class ProductOrderListConverter implements AttributeConverter<ProductOrderListEntity, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ProductOrderListEntity productOrderListEntity) {
        try {
            return objectMapper.writeValueAsString(productOrderListEntity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json객체로 직렬화 실패", e);
        }
    }

    @Override
    public ProductOrderListEntity convertToEntityAttribute(String productOrdersJson) {
        try {
            return objectMapper.readValue(productOrdersJson, new TypeReference<ProductOrderListEntity>() {});
        } catch (IOException e) {
            throw new RuntimeException("역직렬화에 실패", e);
        }
    }
}