package com.yeonieum.orderservice.domain.order.dto.response;

import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.PaymentInformation;
import com.yeonieum.orderservice.domain.order.entity.ProductOrderEntity;
import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.RetrieveOrderInformationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class OrderResponse {

    @Getter
    @Builder
    public static class OfRetrieveForCustomer {
        private MemberInfo memberInfo;
        private Recipient recipient;
        private ProductOrderList productOrderList;
        private String orderDetailId;
        private String orderStatusCode;
        private LocalDateTime orderDateTime;

        @Builder.Default
        private boolean isAvailableProductInformation = true;
        @Builder.Default
        private boolean isAvailableMemberInformation = true;

        public static OfRetrieveForCustomer convertedBy(OrderDetail orderDetail,
                                                        MemberInfo memberInfo,
                                                        boolean isAvailableProductInformation,
                                                        boolean isAvailableMemberService) {
            return OfRetrieveForCustomer.builder()
                    .orderStatusCode(orderDetail.getOrderStatus().getStatusName().getCode())
                    .orderDateTime(orderDetail.getOrderDateTime())
                    .orderDetailId(orderDetail.getOrderDetailId())
                    .memberInfo(memberInfo)
                    .productOrderList(ProductOrderList.convertedBy(orderDetail))
                    .recipient(new Recipient(
                            orderDetail.getRecipient(),
                            orderDetail.getRecipientPhoneNumber(),
                            orderDetail.getDeliveryAddress()
                    )).build();
        }
        public void changeIsAvailableProductInformation(boolean isAvailableProductInformation){
            this.isAvailableProductInformation = isAvailableProductInformation;
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
    public static class OfRetrieveDetailForMember {
        String memberId;
        String orderDetailId;
        Recipient recipient;
        LocalDateTime orderDate;
        @Builder.Default
        String storeName = null;
        String status;
        String orderMemo;
        ProductOrderList productOrderList;
        @Builder.Default
        boolean isAvailableProductInformation = true;


        public static OrderResponse.OfRetrieveDetailForMember convertedBy(OrderDetail orderDetail,
                                                                          String storeName,
                                                                          boolean isAvailableProductInformation) {

            return OrderResponse.OfRetrieveDetailForMember.builder()
                    .memberId(orderDetail.getMemberId())
                    .productOrderList(ProductOrderList.convertedBy(orderDetail))
                    .recipient(new Recipient(
                            orderDetail.getRecipient(),
                            orderDetail.getRecipientPhoneNumber(),
                            orderDetail.getDeliveryAddress()
                    ))
                    .orderDetailId(orderDetail.getOrderDetailId())
                    .orderDate(orderDetail.getOrderDateTime())
                    .status(orderDetail.getOrderStatus().getStatusName().getCode())
                    .storeName(storeName)
                    .orderMemo(orderDetail.getOrderMemo())
                    .isAvailableProductInformation(isAvailableProductInformation)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OfRetrieveForMember {
        String memberId;
        String orderDetailId;
        Recipient recipient;
        LocalDateTime orderDate;
        @Builder.Default
        String storeName = null;
        String status;
        String image;
        String orderMemo;
        int orderedProductCount;
        ProductOrder mainProduct;
        @Builder.Default
        boolean isAvailableProductInformation = true;


        public static OfRetrieveForMember convertedBy(OrderDetail orderDetail,
                                                      RetrieveOrderInformationResponse retrieveOrderInformationResponse,
                                                      boolean isAvailableProductInformation) {

            String image = isAvailableProductInformation ? retrieveOrderInformationResponse.getProductImage() : null;
            String storeName = isAvailableProductInformation ? retrieveOrderInformationResponse.getStoreName() : null;
            return OfRetrieveForMember.builder()
                    .memberId(orderDetail.getMemberId())
                    .mainProduct(ProductOrder.convertedBy(orderDetail.getOrderList().getProductOrderEntityList().get(0)))
                    .recipient(new Recipient(
                            orderDetail.getRecipient(),
                            orderDetail.getRecipientPhoneNumber(),
                            orderDetail.getDeliveryAddress()
                    ))
                    .image(image)
                    .orderDetailId(orderDetail.getOrderDetailId())
                    .orderDate(orderDetail.getOrderDateTime())
                    .status(orderDetail.getOrderStatus().getStatusName().getCode())
                    .storeName(storeName)
                    .orderMemo(orderDetail.getOrderMemo())
                    .orderedProductCount(orderDetail.getOrderList().getProductOrderEntityList().size())
                    .isAvailableProductInformation(isAvailableProductInformation)
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
        String image;
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
        public void changeName(String name){
            this.name = name;
        }
        public void changeImage(String image){
            this.image = image;
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

        public Recipient(String recipient, String recipientPhoneNumber, String recipientAddress) {
            this.recipient = recipient;
            this.recipientPhoneNumber = recipientPhoneNumber;
            this.recipientAddress = recipientAddress;
        }
    }

    @Getter
    @Builder
    public static class MemberInfo{
        String memberId;
        String memberName;
        String memberPhoneNumber;
    }


    @Getter
    @Builder
    public static class OfResultPlaceOrder {
        boolean isPayment;
        int paymentAmount;
        String orderDetailId;
        Long customerId;
    }


    @Getter
    @Builder
    public static class MemberStatistics {
        int ageRange;
        Gender gender;
    }

    @Getter
    @Builder
    public static class ProductOrderCount  {
        private Long productId;
        private Long orderCount;

        public ProductOrderCount(Long productId, Long orderCount) {
            this.productId = productId;
            this.orderCount = orderCount;
        }
    }

    @Getter
    @Builder
    public static class OfResultUpdateStatus {
        String orderDetailId;
        String orderStatusCode;
        List<ProductOrderEntity> productOrderEntityList;

    }
}
