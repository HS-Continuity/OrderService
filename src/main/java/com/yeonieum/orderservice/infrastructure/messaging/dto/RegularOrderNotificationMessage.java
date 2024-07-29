package com.yeonieum.orderservice.infrastructure.messaging.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RegularOrderNotificationMessage {
    private String productName;
    private int productCount;
    private int completedOrderCount;
    private String memberName;
    private String address;
    private LocalDate nextDeliveryDate;
    private String eventType;
    private String phoneNumber;
}
