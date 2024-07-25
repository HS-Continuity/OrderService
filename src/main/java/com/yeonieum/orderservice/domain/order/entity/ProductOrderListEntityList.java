package com.yeonieum.orderservice.domain.order.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.util.List;
@Getter
public class ProductOrderListEntityList {
    List<ProductOrderListEntity> productOrderListEntityList;

    @JsonCreator
    public ProductOrderListEntityList(List<ProductOrderListEntity> productOrderListEntityList) {
        this.productOrderListEntityList = productOrderListEntityList;
    }
}
