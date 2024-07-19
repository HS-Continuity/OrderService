package com.yeonieum.orderservice.domain.order.policy;

import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

public class OrderStatusPolicy {
    public static final Map<OrderStatusCode, RequiredPreviousCondition> orderStatusTransitionRule;
    public static final Map<OrderStatusCode, Set<String>> orderStatusPermission;


    static {
        Map<OrderStatusCode, RequiredPreviousCondition> orderStatusTransitionMap = new EnumMap<>(OrderStatusCode.class);
        // 상태 , 필수 선행 상태
        addTransitionRule(OrderStatusCode.PAYMENT_COMPLETED, OrderStatusCode.PENDING);
        addTransitionRule(OrderStatusCode.CANCELED, OrderStatusCode.PAYMENT_COMPLETED);
        addTransitionRule(OrderStatusCode.PREPARING_PRODUCT, OrderStatusCode.PAYMENT_COMPLETED);
        addTransitionRule(OrderStatusCode.AWAITING_RELEASE, OrderStatusCode.PREPARING_PRODUCT);
        addTransitionRule(OrderStatusCode.SHIPPED, OrderStatusCode.AWAITING_RELEASE);
        addTransitionRule(OrderStatusCode.IN_DELIVERY, OrderStatusCode.SHIPPED);
        addTransitionRule(OrderStatusCode.DELIVERED, OrderStatusCode.IN_DELIVERY);
        addTransitionRule(OrderStatusCode.REFUNDE_REQUEST, OrderStatusCode.DELIVERED);
        addTransitionRule(OrderStatusCode.REFUNDED, OrderStatusCode.REFUNDE_REQUEST);
        orderStatusTransitionRule = Collections.unmodifiableMap(orderStatusTransitionMap);

        Map<OrderStatusCode, Set<String>> orderStatusPermissionMap = new EnumMap(OrderStatusCode.class);
        orderStatusPermissionMap.put(OrderStatusCode.PENDING, Collections.emptySet());
        orderStatusPermissionMap.put(OrderStatusCode.CANCELED, Set.of("USER", "CUSTOMER"));
        orderStatusPermissionMap.put(OrderStatusCode.PAYMENT_COMPLETED, Collections.emptySet());
        orderStatusPermissionMap.put(OrderStatusCode.PREPARING_PRODUCT, Set.of("CUSTOMER"));
        orderStatusPermissionMap.put(OrderStatusCode.AWAITING_RELEASE, Set.of("CUSTOMER"));
        orderStatusPermissionMap.put(OrderStatusCode.SHIPPED, Set.of("CUSTOMER"));
        orderStatusPermissionMap.put(OrderStatusCode.IN_DELIVERY, Collections.emptySet());
        orderStatusPermissionMap.put(OrderStatusCode.DELIVERED, Collections.emptySet());
        orderStatusPermissionMap.put(OrderStatusCode.REFUNDE_REQUEST, Set.of("USER"));
        orderStatusPermissionMap.put(OrderStatusCode.REFUNDED, Set.of("CUSTOMER"));
        orderStatusPermission = Collections.unmodifiableMap(orderStatusPermissionMap);
    }


    private static void addTransitionRule(OrderStatusCode statusCode, OrderStatusCode requiredPreviousStatus) {
        Set<OrderStatusCode> requiredPreviousConditionSet = EnumSet.copyOf(Arrays.asList(requiredPreviousStatus));
        RequiredPreviousCondition condition = RequiredPreviousCondition.builder()
                .requiredPreviosConditionSet(requiredPreviousConditionSet)
                .build();
        orderStatusTransitionRule.put(statusCode, condition);
    }

    public static Map<OrderStatusCode, RequiredPreviousCondition> getOrderStatusTransitionRule() {
        return orderStatusTransitionRule;
    }

    @Getter
    @Builder
    public static class RequiredPreviousCondition {
        Set<OrderStatusCode> requiredPreviosConditionSet;
    }
}
