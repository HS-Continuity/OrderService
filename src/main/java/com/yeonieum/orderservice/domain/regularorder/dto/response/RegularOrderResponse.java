package com.yeonieum.orderservice.domain.regularorder.dto.response;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplication;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegularOrderResponse {
    @Getter
    @Builder
    public static class OfRetrieve {
        Long regularOrderId;
        ProductOrder productOrder;
        int orderProductAmount;
        LocalDate orderDate;
        String regularDeliveryStatus;
        String orderMemo;
        String storeName;

        public static OfRetrieve convertedBy(RegularDeliveryApplication application,
                                             Map<Long, ProductOrder> productOrderMap,
                                             boolean isAvailableProductService) {
            ProductOrder productOrder = isAvailableProductService ? productOrderMap.get(application.getMainProductId()) : null;
            String storeName = isAvailableProductService ? productOrderMap.get(application.getMainProductId()).getStoreName() : null;
            return OfRetrieve.builder()
                    .regularOrderId(application.getRegularDeliveryApplicationId())
                    .productOrder(productOrder)
                    .orderProductAmount(application.getOrderedProductCount())
                    .orderDate(application.getNextDeliveryDate())
                    .regularDeliveryStatus(application.getRegularDeliveryStatus().getStatusName())
                    .orderMemo(application.getOrderMemo())
                    .storeName(storeName)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OfRetrieveDetails {
        Long regularOrderId;
        ProductOrderList productOrderList;
        Recipient recipient;
        DeliveryPeriod deliveryPeriod;
        LocalDate nextDeliveryDate;
        String regularDeliveryStatus;
        String orderMemo;
        String storeName;
        String status;
        int paymentAmount;
        boolean isAvailableProductService;

        public static OfRetrieveDetails convertedBy(RegularDeliveryApplication application,
                                                    Map<Long, ProductOrder> productOrderMap,
                                                    boolean isAvailableProductService) {

            ProductOrderList orderList =
                    isAvailableProductService ? ProductOrderList.builder()
                            .productOrderList(productOrderMap.values().stream().collect(Collectors.toList())
                            ).build()  :  null;

            String storeName = isAvailableProductService ? productOrderMap.get(application.getMainProductId()).getStoreName() : null;
            return OfRetrieveDetails.builder()
                    .regularOrderId(application.getRegularDeliveryApplicationId())
                    .productOrderList(orderList)
                    .recipient(Recipient.convertedBy(application))
                    .deliveryPeriod(DeliveryPeriod.convertedBy(application))
                    .nextDeliveryDate(application.getNextDeliveryDate())
                    .orderMemo(application.getOrderMemo())
                    .regularDeliveryStatus(application.getRegularDeliveryStatus().getStatusName())
                    .storeName(storeName)
                    .status(application.getRegularDeliveryStatus().getStatusName())
                    .isAvailableProductService(isAvailableProductService)
                    .paymentAmount(orderList.getProductOrderList().stream().map(ProductOrder::getFinalPrice).reduce(0, Integer::sum))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class OfRetrieveDailySummary {
        private Long productCount;
        private Long mainProductId;
        private Long regularDelivaryApplicationId;
        private LocalDate days;
        private String productName;
        private String memberId;
        private OrderResponse.MemberInfo memberInfo;
        private boolean isAvailableProductService;
        private boolean isAvailableMemberService;

        public OfRetrieveDailySummary(Long productCount, Long mainProductId, Long regularDelivaryApplicationId, String memberId, LocalDate days) {
            this.productCount = productCount;
            this.mainProductId = mainProductId;
            this.memberId = memberId;
            this.regularDelivaryApplicationId = regularDelivaryApplicationId;
            this.days = days;
        }

        public void bindProductName(String productName) {
            this.productName = productName;
        }
        public void setAvailableProductService (boolean isAvailableProductService) {
            this.isAvailableProductService = isAvailableProductService;
        }
        public void bindMemberInfo(OrderResponse.MemberInfo memberInfo) {
            this.memberInfo = memberInfo;
        }
        public void setAvailableMemberService (boolean isAvailableMemberService) {
            this.isAvailableMemberService = isAvailableMemberService;
        }
    }



    @Getter
    public static class OfRetrieveDailyDetail {
        private Long regularDelivaryApplicationId;
        private LocalDate today;
        private Long reservationCount;
        private Long productId;
        private String productName;
        private String memberId;
        private OrderResponse.MemberInfo memberInfo;
        private boolean isAvailableProductService;
        private boolean isAvailableMemberService;

        public OfRetrieveDailyDetail(Long regularDelivaryApplicationId, LocalDate today, Long reservationCount,String memberId, Long productId) {
            this.regularDelivaryApplicationId = regularDelivaryApplicationId;
            this.today = today;
            this.reservationCount = reservationCount;
            this.memberId = memberId;
            this.productId = productId;
        }
        public void bindProductName(String productName) {
            this.productName = productName;
        }

        public void setAvailableProductService(boolean isAvailableProductService) {
            this.isAvailableProductService = isAvailableProductService;
        }

        public void setAvailableMemberService(boolean isAvailableMemberService) {
            this.isAvailableMemberService = isAvailableMemberService;
        }
        public void bindMemberInfo(OrderResponse.MemberInfo memberInfo) {
            this.memberInfo = memberInfo;
        }
    }


    @Getter
    @Builder
    public static class Recipient {
        private String recipient;
        private String recipientPhoneNumber;
        private String recipientAddress;
        private String orderMemo;

        public static Recipient convertedBy(RegularDeliveryApplication application) {
            return Recipient.builder()
                    .recipient(application.getRecipient())
                    .recipientPhoneNumber(application.getRecipientPhoneNumber())
                    .recipientAddress(application.getAddress())
                    .orderMemo(application.getOrderMemo())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductOrderList {
        List<ProductOrder> productOrderList;
    }

    @Getter
    @Builder
    public static class DeliveryPeriod {
        LocalDate startDate;
        LocalDate endDate;
        int deliveryCycle; // 배송 주기
        List<String> deliveryDayOfWeeks;// 배송요일

        public static DeliveryPeriod convertedBy(RegularDeliveryApplication application) {
            return DeliveryPeriod.builder()
                    .startDate(application.getStartDate())
                    .endDate(application.getEndDate())
                    .deliveryCycle(application.getCycle())
                    .deliveryDayOfWeeks(application.getRegularDeliveryApplicationDayList().stream()
                            .map(applicationDay -> applicationDay.getDayCode().getStoredDayValue())
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductOrder {
        Long productId;
        String productName;
        String productImage;
        String storeName;
        int originPrice;
        int finalPrice;
        int productAmount;

        public void changeProductAmount(int productAmount) {
            this.productAmount = productAmount;
        }
    }

    @Getter
    @Builder
    public static class OfSuccess {
        private Long regularDeliveryApplicationId;
        private Long customerId;
        private String memberId;
    }
}
