package com.yeonieum.orderservice.domain.delivery.dto;

import com.yeonieum.orderservice.domain.order.entity.ProductOrderListEntityList;
import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
@Getter
@Builder
public class DeliveryGenuineResponse {
    private Long deliveryId;
    private String shipmentNumber;
    private DeliveryStatusCode deliveryStatusCode;
    private LocalDate startDeliveryDate;
    private String representativeOrderId;
    private Long additionalOrderCount;
    private ProductOrderListEntityList productOrderListEntityLists;
    private String memberId;
}
