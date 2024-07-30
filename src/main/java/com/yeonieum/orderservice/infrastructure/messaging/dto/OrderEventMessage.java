package com.yeonieum.orderservice.infrastructure.messaging.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderEventMessage {
    private String memberId;
    private Long customerId;
    private String orderDetailId;
    private String eventType;
    private String topic;
}
