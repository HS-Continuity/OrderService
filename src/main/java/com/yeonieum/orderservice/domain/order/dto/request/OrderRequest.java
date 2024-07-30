package com.yeonieum.orderservice.domain.order.dto.request;

import com.yeonieum.orderservice.domain.order.entity.*;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderRequest {

    @Getter
    @NoArgsConstructor
    public static class OfRetrieve {
        OrderStatusCode orderStatusCode;

    }

    @Getter
    @NoArgsConstructor
    public static class OfUpdateOrderStatus {
        String orderId;
        OrderStatusCode orderStatusCode;
    }

    @Getter
    @NoArgsConstructor
    public static class OfBulkUpdateOrderStatus {
        List<String> orderIds;
        OrderStatusCode orderStatusCode;
    }

    @Getter
    @NoArgsConstructor
    public static class OfUpdateProductOrderStatus {
        String orderId;
        Long productId;
        OrderStatusCode orderStatusCode;
    }

    @Getter
    @NoArgsConstructor
    public static class OfCreation {
        Long customerId;
        Long memberCouponId;
        String storeName;
        ProductOrderList productOrderList;
        Recipient recipient;
        int originProductAmount;
        int totalDiscountAmount;
        int paymentAmount;
        int deliveryFee;
        String orderMemo;
        private int paymentCardId;

        public OrderDetail toOrderDetailEntity(String memberId,OrderStatus orderStatus, String orderDetailId) {
            return OrderDetail.builder()
                    .orderDetailId(orderDetailId)
                    .customerId(this.getCustomerId())
                    .orderMemo(this.getOrderMemo())
                    .deliveryAddress(this.getRecipient().getRecipientAddress())
                    .recipient(this.getRecipient().getRecipient())
                    .recipientPhoneNumber(this.getRecipient().getRecipientPhoneNumber())
                    .storeName(this.getStoreName())
                    .memberId(memberId)
                    .orderDateTime(LocalDateTime.now())
                    .mainProductId(this.getProductOrderList().getProductOrderList().get(0).getProductId())
                    .orderList(this.getProductOrderList().toEntity(orderStatus.getStatusName())) // json 컨버터 객체 생성하기
                    .orderStatus(orderStatus)
                    .build();
        }

        public PaymentInformation toPaymentInformationEntity(OrderDetail orderDetail,
                                                             String cardNumber,
                                                             int canceledDiscountAmount,
                                                             int canceledPaymentAmount,
                                                             int canceledOriginProductPrice) {
            return PaymentInformation.builder()
                    .orderDetail(orderDetail)
                    .deliveryFee(this.getDeliveryFee())
                    .discountAmount(this.getTotalDiscountAmount() - canceledDiscountAmount)
                    .cardNumber(cardNumber)
                    .paymentAmount(this.getPaymentAmount() - canceledPaymentAmount)
                    .originProductPrice(this.getOriginProductAmount() - canceledOriginProductPrice)
                    .build();
        }

        public void changePaymentAmount(int paymentAmount) {
            this.paymentAmount = paymentAmount;
        }
        public void changeProductOrderList(ProductOrderList productOrderList) {
            this.productOrderList = productOrderList;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentCard {
        String cardNumber;
        String cardCompany;
    }

    @Getter
    @NoArgsConstructor
    public static class Recipient {
        String recipient;
        String recipientPhoneNumber;
        String recipientAddress;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductOrder {
        Long productId;
        String name;
        int originPrice;
        int discountAmount;
        int finalPrice;
        int quantity;
        OrderStatusCode status ;
        // 상품 주문 상태
        public void changeStatus(OrderStatusCode status) {
            this.status = status;
        }

        public ProductOrderEntity toEntity(OrderStatusCode status) {
            return ProductOrderEntity.builder()
                    .productId(this.productId)
                    .name(this.getName())
                    .originPrice(this.getOriginPrice())
                    .discountAmount(this.getDiscountAmount())
                    .finalPrice(this.getFinalPrice())
                    .quantity(this.getQuantity())
                    .status(status)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ProductOrderList {
        List<ProductOrder> productOrderList;

        public ProductOrderListEntity toEntity(OrderStatusCode status) {
            return ProductOrderListEntity.builder()
                    .productOrderEntityList(this.getProductOrderList().stream().map(productOrder ->
                            productOrder.toEntity(status)).collect(Collectors.toList()))
                    .build();
        }

        public ProductOrderList(ProductOrderList productOrderList) {
            this.productOrderList = productOrderList.getProductOrderList();
        }
    }
}
