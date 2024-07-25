package com.yeonieum.orderservice.domain.order.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.util.List;
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ProductOrderListEntity {
    List<ProductOrderEntity> productOrderEntityList;

    @JsonCreator
    public ProductOrderListEntity(List<ProductOrderEntity> productOrderEntityList) {
        this.productOrderEntityList = productOrderEntityList;
    }
}
