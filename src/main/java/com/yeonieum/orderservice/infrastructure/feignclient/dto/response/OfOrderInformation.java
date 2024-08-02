package com.yeonieum.orderservice.infrastructure.feignclient.dto.response;

import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfOrderInformation {
    private Long productId;
    private String productName;
    private String name;
    private int originPrice;
    private int regularPrice;
    private double finalPrice;
    private int quantity;
    boolean isAvailable;
    @Builder.Default
    OrderStatusCode status = OrderStatusCode.PENDING;

    public void cancelOrder() {
        // isAvailable 이 false일 경우 status를 CANCELED로
        if (!isAvailable) {
            this.status = OrderStatusCode.CANCELED;
        }
    }
}