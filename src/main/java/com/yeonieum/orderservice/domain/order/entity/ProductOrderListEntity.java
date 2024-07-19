package com.yeonieum.orderservice.domain.order.entity;

import lombok.*;

import java.util.List;
@Getter

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductOrderListEntity {
    List<ProductOrderEntity> productOrderEntityList;
}
