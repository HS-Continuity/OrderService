package com.yeonieum.orderservice.domain.release.dto;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.release.entity.Release;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class ReleaseResponse {

    @Getter
    @Builder
    public static class OfRetrieve {
        String orderId;
        OrderResponse.MemberInfo memberInfo;
        LocalDate startDeliveryDate;
        OrderResponse.Recipient recipient;
        OrderResponse.ProductOrderList productOrderList;

        public static OfRetrieve convertedBy(OrderDetail orderDetail, Release release, OrderResponse.MemberInfo memberInfo) {
            return OfRetrieve.builder()
                    .orderId(orderDetail.getOrderDetailId())
                    .memberInfo(memberInfo)
                    .startDeliveryDate(release.getStartDeliveryDate())
                    .recipient(new OrderResponse.Recipient(
                            orderDetail.getRecipient(),
                            orderDetail.getRecipientPhoneNumber(),
                            orderDetail.getDeliveryAddress()
                    ))
                    .productOrderList(OrderResponse.ProductOrderList.convertedBy(orderDetail))
                    .build();
        }
    }
}
