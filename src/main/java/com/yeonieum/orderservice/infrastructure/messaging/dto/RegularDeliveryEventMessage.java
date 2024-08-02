package com.yeonieum.orderservice.infrastructure.messaging.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegularDeliveryEventMessage {
    private String memberId;
    private Long customerId;
    private Long regularDeliveryId;
    private String eventType;
    private String topic;
}
