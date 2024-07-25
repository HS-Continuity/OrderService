package com.yeonieum.orderservice.domain.delivery.dto;

import lombok.Builder;
import lombok.Getter;

import java.sql.Date;

@Getter
@Builder
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
        return new DeliveryResponse(
                (Long) result[0],
                (String) result[1],
                (Long) result[2],
                (Date) result[3],
                (String) result[4],
                (Long) result[5],
                (String) result[6],
                (String) result[7]
        );
    }
}
