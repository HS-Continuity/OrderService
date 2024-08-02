package com.yeonieum.orderservice.domain.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Date;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryResponse {
    private Long deliveryId;
    private String shipmentNumber;
    private Long deliveryStatusCode;
    private Date startDeliveryDate;
    private String representativeOrderId;
    private Long additionalOrderCount;
    private String productOrderListEntityList;
    private String memberId;

    public static DeliveryResponse convertedBy(Object[] result) {
        return DeliveryResponse.builder()
                .deliveryId((Long) result[0])
                .shipmentNumber((String) result[1])
                .deliveryStatusCode((Long) result[2])
                .startDeliveryDate((Date) result[3])
                .representativeOrderId((String) result[4])
                .additionalOrderCount((Long) result[5])
                .productOrderListEntityList((String) result[6])
                .memberId((String) result[7])
                .build();
    }
}

