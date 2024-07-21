package com.yeonieum.orderservice.domain.order.dto.response;

import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSummaryResponse {
    private OrderStatusCode statusName;
    private Long count;

    public OrderSummaryResponse(OrderStatusCode statusName, Long count) {
        this.statusName = statusName;
        this.count = count;
    }
}
