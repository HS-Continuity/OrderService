package com.yeonieum.orderservice.domain.delivery.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static DeliveryGenuineResponse convertedBy(DeliveryResponse deliveryResponse, DeliveryStatusCode deliveryStatusCode, ObjectMapper objectMapper) {
        try {
            String jsonInput = "{\"productOrderListEntityList\":[" + deliveryResponse.getProductOrderListEntityList() + "]}";

            ProductOrderListEntityList productOrderLists = objectMapper.readValue(jsonInput, ProductOrderListEntityList.class);

            return DeliveryGenuineResponse.builder()
                    .deliveryId(deliveryResponse.getDeliveryId())
                    .shipmentNumber(deliveryResponse.getShipmentNumber())
                    .deliveryStatusCode(deliveryStatusCode)
                    .startDeliveryDate(deliveryResponse.getStartDeliveryDate().toLocalDate())
                    .representativeOrderId(deliveryResponse.getRepresentativeOrderId())
                    .additionalOrderCount(deliveryResponse.getAdditionalOrderCount())
                    .memberId(deliveryResponse.getMemberId())
                    .productOrderListEntityLists(productOrderLists)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 처리 중 오류 발생", e);
        } catch (RuntimeException e) {
            throw new RuntimeException("DeliveryGenuineResponse 구성 중 오류 발생", e);
        }
    }
}

