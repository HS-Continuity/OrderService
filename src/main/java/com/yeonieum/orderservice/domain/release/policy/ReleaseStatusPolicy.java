package com.yeonieum.orderservice.domain.release.policy;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import lombok.Builder;
import lombok.Getter;

import java.util.*;

public class ReleaseStatusPolicy {
    public static final Map<ReleaseStatusCode, RequiredPreviousCondition> releaseStatusTransitionRule;
    public static final Map<ReleaseStatusCode, Set<String>> releaseStatusPermission;

    static {
        Map<ReleaseStatusCode, RequiredPreviousCondition> releaseStatusTransitionMap = new EnumMap<>(ReleaseStatusCode.class);

        //출고 보류는 출고 대기가 필수 선행 상태가 되어야 한다.
        addTransitionRule(ReleaseStatusCode.HOLD_RELEASE, ReleaseStatusCode.AWAITING_RELEASE);

        //출고 완료는 출고 대기 혹은 출고 보류가 필수 선행 상태가 되어야 한다.
        addTransitionRule(ReleaseStatusCode.RELEASE_COMPLETED, ReleaseStatusCode.AWAITING_RELEASE);
        addTransitionRule(ReleaseStatusCode.RELEASE_COMPLETED, ReleaseStatusCode.HOLD_RELEASE);
        releaseStatusTransitionRule = Collections.unmodifiableMap(releaseStatusTransitionMap);

        Map<ReleaseStatusCode, Set<String>> releaseStatusCodeSetMap = new EnumMap(ReleaseStatusCode.class);
        releaseStatusCodeSetMap.put(ReleaseStatusCode.HOLD_RELEASE, Set.of("CUSTOMER"));
        releaseStatusCodeSetMap.put(ReleaseStatusCode.RELEASE_COMPLETED,  Set.of("CUSTOMER"));
        releaseStatusPermission = Collections.unmodifiableMap(releaseStatusCodeSetMap);
    }

    private static void addTransitionRule(ReleaseStatusCode statusCode, ReleaseStatusCode requiredPreviousStatus) {
        Set<ReleaseStatusCode> requiredPreviousConditionSet = EnumSet.copyOf(Arrays.asList(requiredPreviousStatus));
        com.yeonieum.orderservice.domain.release.policy.ReleaseStatusPolicy.RequiredPreviousCondition condition = RequiredPreviousCondition.builder()
                .requiredPreviosConditionSet(requiredPreviousConditionSet)
                .build();
        releaseStatusTransitionRule.put(statusCode, condition);
    }

    public static Map<ReleaseStatusCode, RequiredPreviousCondition> getReleaseStatusTransitionRule() {
        return releaseStatusTransitionRule;
    }

    @Getter
    @Builder
    public static class RequiredPreviousCondition {
        Set<ReleaseStatusCode> requiredPreviosConditionSet;
    }
}
