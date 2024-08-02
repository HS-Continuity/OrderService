package com.yeonieum.orderservice.domain.delivery.dto;

import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliverySummaryResponse {
    DeliveryStatusCode statusName;
    Long count;
}
