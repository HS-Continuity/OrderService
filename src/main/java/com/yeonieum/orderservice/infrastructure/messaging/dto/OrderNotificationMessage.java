package com.yeonieum.orderservice.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.RetrieveOrderInformationResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderNotificationMessage {
    // 상품 정보
    private String productName;
    private int productCount;

    // 주문 정보
    private String memberName;
    private String orderNumber;

    // 이벤트
    private String eventType;

    private String phoneNumber;

    // 생성자
    @JsonCreator
    public OrderNotificationMessage(@JsonProperty("productName") String productName,
                                    @JsonProperty("productCount") int productCount,
                                    @JsonProperty("memberName") String memberName,
                                    @JsonProperty("orderNumber") String orderNumber,
                                    @JsonProperty("eventType") String eventType,
                                    @JsonProperty("phoneNumber") String phoneNumber) {
        this.productName = productName;
        this.productCount = productCount;
        this.memberName = memberName;
        this.orderNumber = orderNumber;
        this.eventType = eventType;
        this.phoneNumber = phoneNumber;
    }


    public static OrderNotificationMessage convertedBy(OrderDetail orderDetail,
                                                       RetrieveOrderInformationResponse productInformation,
                                                       String phoneNumber,
                                                       String memberName,
                                                       String eventType) {
        return OrderNotificationMessage.builder()
                .orderNumber(orderDetail.getOrderDetailId())
                .memberName(memberName)
                .productName(productInformation.getProductName())
                .eventType(eventType)
                .productCount(orderDetail.getOrderList().getProductOrderEntityList().size())
                .phoneNumber(phoneNumber)
                .build();
    }
}
