package com.yeonieum.orderservice.domain.order.dto.response;

import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.PaymentInformation;
import com.yeonieum.orderservice.domain.order.entity.ProductOrderEntity;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


public class OrderResponse {
    @Getter
    @Builder
    public static class OfRetrieveForCustomer {
        MemberInfo memberInfo;
        Recipient recipient;
        ProductOrderList productOrderList;

        public static OfRetrieveForCustomer convertedBy(OrderDetail orderDetail, MemberInfo memberInfo) {
            return OfRetrieveForCustomer.builder()
                    .memberInfo(memberInfo)
                    .productOrderList(ProductOrderList.convertedBy(orderDetail))
                    .recipient(new Recipient(
                            orderDetail.getRecipient(),
                            orderDetail.getRecipientPhoneNumber(),
                            orderDetail.getDeliveryAddress()
                    )).build();
        }
    }

    @Getter
    @Builder
    public static class OfRetrievePayment {
        private String cardNumber;
        private int deliveryFee;
        private int discountAmount;
        private int originProductPrice;
        private int paymentAmount;


        public static OfRetrievePayment convertedBy(PaymentInformation paymentInformation) {
            return OfRetrievePayment.builder()
                    .cardNumber(paymentInformation.getCardNumber())
                    .deliveryFee(paymentInformation.getDeliveryFee())
                    .discountAmount(paymentInformation.getDiscountAmount())
                    .originProductPrice(paymentInformation.getOriginProductPrice())
                    .paymentAmount(paymentInformation.getPaymentAmount())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OfRetrieveForMember {
        String memberId;
        Recipient recipient;
        LocalDateTime orderDate;
        @Builder.Default
        String storeName = null;
        String status;
        ProductOrderList productOrderList;


        public static OfRetrieveForMember convertedBy(OrderDetail orderDetail, String storeName) {
            return OfRetrieveForMember.builder()
                    .memberId(orderDetail.getMemberId())
                    .productOrderList(ProductOrderList.convertedBy(orderDetail))
                    .recipient(new Recipient(
                            orderDetail.getRecipient(),
                            orderDetail.getRecipientPhoneNumber(),
                            orderDetail.getDeliveryAddress()
                    ))
                    .orderDate(orderDetail.getOrderDateTime())
                    .status(orderDetail.getOrderStatus().getStatusName().getCode())
                    .storeName(storeName)
                    .build();
        }

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductOrder {
        Long productId;
        String name;
        int originPrice; // 상품금액
        int discountAmount; // 상품 할인액
        int finalPrice; // 최종상품금액
        int quantity;
        @Builder.Default
        OrderStatusCode status = OrderStatusCode.PENDING;
        // 상품 주문 상태
        public void changeStatus(OrderStatusCode status) {
            this.status = status;
        }

        public static ProductOrder convertedBy(ProductOrderEntity productOrderEntity) {
            return ProductOrder.builder()
                    .productId(productOrderEntity.getProductId())
                    .name(productOrderEntity.getName())
                    .originPrice(productOrderEntity.getOriginPrice())
                    .discountAmount(productOrderEntity.getDiscountAmount())
                    .finalPrice(productOrderEntity.getFinalPrice())
                    .quantity(productOrderEntity.getQuantity())
                    .status(productOrderEntity.getStatus())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductOrderList implements Serializable {
        List<ProductOrder> productOrderList;

        public static ProductOrderList convertedBy(OrderDetail orderDetail) {
            return ProductOrderList.builder()
                    .productOrderList(orderDetail.getOrderList().getProductOrderEntityList().stream()
                            .map(ProductOrder::convertedBy)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Product {
        String name;
        int originPrice;
        int discountAmount;
        int finalPrice;
        int quantity;
        String status;
    }

    @Getter
    @Builder
    public static class Recipient {
        String recipient;
        String recipientPhoneNumber;
        String recipientAddress;
    }

    @Getter
    @Builder
    public static class MemberInfo{
        String memberId;
        String memberName;
        String memberPhoneNumber;
    }
}
