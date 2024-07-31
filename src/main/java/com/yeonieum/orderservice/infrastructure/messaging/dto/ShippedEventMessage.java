package com.yeonieum.orderservice.infrastructure.messaging.dto;

import com.yeonieum.orderservice.domain.order.entity.ProductOrderEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ShippedEventMessage {
    private String orderDetailId;
    private Long productId;
    private int quantity;
    private String shippedAt;

    public ShippedEventMessage(String orderDetailId, Long productId, int quantity) {
        this.orderDetailId = orderDetailId;
        this.productId = productId;
        this.quantity = quantity;
        this.shippedAt = LocalDateTime.now().toString();
    }


    public static ShippedEventMessage convertedBy(String orderDetailId, ProductOrderEntity productOrderEntity) {
        return new ShippedEventMessage(orderDetailId, productOrderEntity.getProductId(), productOrderEntity.getQuantity());
    }
}
