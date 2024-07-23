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
        OrderResponse.MemberInfo memberInfo;
        LocalDate startDeliveryDate;
        OrderResponse.ProductOrderList productOrderList;

        public static OfRetrieve convertedBy(OrderDetail orderDetail, Release release, OrderResponse.MemberInfo memberInfo) {
            return OfRetrieve.builder()
                    .memberInfo(memberInfo)
                    .startDeliveryDate(release.getStartDeliveryDate())
                    .productOrderList(OrderResponse.ProductOrderList.convertedBy(orderDetail))
                    .build();
        }
    }
}
