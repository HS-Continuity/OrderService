package com.yeonieum.orderservice.domain.regularorder.dto.request;

import com.yeonieum.orderservice.domain.order.dto.request.OrderRequest;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplication;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplicationDay;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryReservation;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryStatus;
import com.yeonieum.orderservice.global.enums.DayOfWeek;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RegularOrderRequest {

    @Getter
    @NoArgsConstructor
    public static class OfPostPone {
        int rounds;
        Long productId;
    }


    @Getter
    @NoArgsConstructor
    public static class OfCreation {
        private Long customerId;
        private String memberId;
        private Long memberCouponId;
        private String orderMemo;
        private Long paymentCardId;
        private ProductOrderList productOrderList;
        private DeliveryPeriod deliveryPeriod;
        private Recipient recipient;

        public RegularDeliveryApplication toApplicationEntity(RegularDeliveryStatus regularDeliveryStatus) {
            return RegularDeliveryApplication.builder()
                    .orderMemo(this.orderMemo)
                    .startDate(this.deliveryPeriod.startDate)
                    .endDate(this.deliveryPeriod.endDate)
                    .cycle(this.deliveryPeriod.getDeliveryCycle())
                    .recipient(this.recipient.getRecipient())
                    .recipientPhoneNumber(this.recipient.getRecipientPhoneNumber())
                    .memberId(this.memberId)
                    .memberPaymentCardId(this.paymentCardId)
                    .address(this.recipient.getRecipientAddress())
                    .customerId(this.customerId)
                    .mainProductId(this.productOrderList.getProductOrderList().get(0).getProductId())
                    .nextDeliveryDate(this.deliveryPeriod.startDate)
                    .completedRounds(0)
                    .regularDeliveryStatus(regularDeliveryStatus)
                    .build();
        }

        // 단일 배송 날짜에 대한 엔티티 리스트 생성 메서드
        public List<RegularDeliveryReservation> toReservationEntity(LocalDate deliveryDay,
                                                                    RegularDeliveryApplication application,
                                                                    RegularDeliveryStatus status) {
            return productOrderList.getProductOrderList().stream()
                    .map(productOrder -> RegularDeliveryReservation.builder()
                            .regularDeliveryApplication(application)
                            .regularDeliveryStatus(status)
                            .startDate(deliveryDay)
                            .quantity(productOrder.getQuantity())
                            .productId(productOrder.getProductId())
                            .memberId(this.memberId)
                            .build())
                    .collect(Collectors.toList());
        }

        // 여러 배송 날짜에 대한 엔티티 리스트 생성 메서드
        public List<RegularDeliveryReservation> toReservationEntityList(Set<LocalDate> deliveryDates,
                                                                        RegularDeliveryApplication application,
                                                                        RegularDeliveryStatus status) {
            return deliveryDates.stream()
                    .flatMap(deliveryDay -> toReservationEntity(deliveryDay, application, status).stream())
                    .collect(Collectors.toList());
        }

        public List<RegularDeliveryApplicationDay> toApplicationDayEnityList(RegularDeliveryApplication application) {
            return this.getDeliveryPeriod().deliveryDayOfWeeks.stream().map(
                    dayOfWeek -> {
                        return RegularDeliveryApplicationDay.builder()
                                .dayCode(dayOfWeek)
                                .regularDeliveryApplication(application)
                                .build();
                    }
            ).collect(Collectors.toList());
        }
    }

    @Getter
    @NoArgsConstructor
    public static class DeliveryPeriod {
        LocalDate startDate;
        LocalDate endDate;
        int deliveryCycle;
        List<DayOfWeek> deliveryDayOfWeeks;
    }

    @Getter
    @NoArgsConstructor
    public static class Recipient {
        private String recipient;
        private String recipientPhoneNumber;
        private String recipientAddress;
    }

    @Getter
    @Builder
    public static class ProductOrder {
        private Long productId;
        private int quantity;
    }

    @Getter
    @NoArgsConstructor
    public static class ProductOrderList {
        List<ProductOrder> productOrderList;
    }
}
