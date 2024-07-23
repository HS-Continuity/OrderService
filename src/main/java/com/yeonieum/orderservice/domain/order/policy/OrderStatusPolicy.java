package com.yeonieum.orderservice.domain.order.policy;

import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
@Component
public class OrderStatusPolicy {
    @Getter
    private Map<OrderStatusCode, RequiredPreviousCondition> orderStatusTransitionRule;
    @Getter
    private Map<OrderStatusCode, Set<String>> orderStatusPermission;
    private Map<OrderStatusCode, RequiredPreviousCondition> orderStatusTransitionMap = new EnumMap<>(OrderStatusCode.class);

    @PostConstruct
    public void init() {
        addTransitionRule(OrderStatusCode.PAYMENT_COMPLETED, Arrays.asList(OrderStatusCode.PENDING));
        addTransitionRule(OrderStatusCode.CANCELED, Arrays.asList(OrderStatusCode.PAYMENT_COMPLETED));
        addTransitionRule(OrderStatusCode.PREPARING_PRODUCT, Arrays.asList(OrderStatusCode.PAYMENT_COMPLETED));
        addTransitionRule(OrderStatusCode.AWAITING_RELEASE, Arrays.asList(OrderStatusCode.PREPARING_PRODUCT));
        addTransitionRule(OrderStatusCode.SHIPPED, Arrays.asList(OrderStatusCode.AWAITING_RELEASE));
        addTransitionRule(OrderStatusCode.IN_DELIVERY, Arrays.asList(OrderStatusCode.SHIPPED));
        addTransitionRule(OrderStatusCode.DELIVERED, Arrays.asList(OrderStatusCode.IN_DELIVERY));
        addTransitionRule(OrderStatusCode.REFUND_REQUEST, Arrays.asList(OrderStatusCode.DELIVERED));
        addTransitionRule(OrderStatusCode.REFUNDED, Arrays.asList(OrderStatusCode.REFUND_REQUEST));
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
        orderStatusPermissionMap.put(OrderStatusCode.REFUND_REQUEST, Set.of("USER"));
        orderStatusPermissionMap.put(OrderStatusCode.REFUNDED, Set.of("CUSTOMER"));
        orderStatusPermission = Collections.unmodifiableMap(orderStatusPermissionMap);
    }


    private void addTransitionRule(OrderStatusCode statusCode, List<OrderStatusCode> requiredPreviousStatusList) {
        Set<OrderStatusCode> requiredPreviousConditionSet = EnumSet.copyOf(requiredPreviousStatusList);
        RequiredPreviousCondition condition = RequiredPreviousCondition.builder()
                .requiredPreviosConditionSet(requiredPreviousConditionSet)
                .build();
        orderStatusTransitionMap.put(statusCode, condition);
    }

    @Getter
    @Builder
    public static class RequiredPreviousCondition {
        Set<OrderStatusCode> requiredPreviosConditionSet;
    }
}
