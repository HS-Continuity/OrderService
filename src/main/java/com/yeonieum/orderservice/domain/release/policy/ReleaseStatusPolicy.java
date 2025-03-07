package com.yeonieum.orderservice.domain.release.policy;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ReleaseStatusPolicy {

    //출고 상태 전환 규칙 Map
    @Getter
    public Map<ReleaseStatusCode, RequiredPreviousCondition> releaseStatusTransitionRule;

    //출고 상태 변경 권한 Map
    @Getter
    public Map<ReleaseStatusCode, Set<String>> releaseStatusPermission;


    private Map<ReleaseStatusCode, RequiredPreviousCondition> releaseStatusTransitionMap = new EnumMap<>(ReleaseStatusCode.class);
    private Map<ReleaseStatusCode, Set<String>> releaseStatusCodeSetMap = new EnumMap(ReleaseStatusCode.class);

    @PostConstruct
    public void init() {
        //출고 보류는 출고 대기가 필수 선행 상태가 되어야 한다.
        //출고 완료는 출고 대기 혹은 출고 보류가 필수 선행 상태가 되어야 한다.

        addTransitionRule(ReleaseStatusCode.HOLD_RELEASE,Arrays.asList(ReleaseStatusCode.AWAITING_RELEASE));
        addTransitionRule(ReleaseStatusCode.RELEASE_COMPLETED, Arrays.asList(ReleaseStatusCode.AWAITING_RELEASE, ReleaseStatusCode.HOLD_RELEASE));
        addTransitionRule(ReleaseStatusCode.COMBINED_PACKAGING_COMPLETED, Arrays.asList(ReleaseStatusCode.AWAITING_RELEASE, ReleaseStatusCode.HOLD_RELEASE));
        releaseStatusTransitionRule = Collections.unmodifiableMap(releaseStatusTransitionMap);

        releaseStatusCodeSetMap.put(ReleaseStatusCode.HOLD_RELEASE, Set.of("ROLE_CUSTOMER"));
        releaseStatusCodeSetMap.put(ReleaseStatusCode.RELEASE_COMPLETED,  Set.of("ROLE_CUSTOMER"));
        releaseStatusCodeSetMap.put(ReleaseStatusCode.COMBINED_PACKAGING_COMPLETED,  Set.of("ROLE_CUSTOMER"));
        releaseStatusPermission = Collections.unmodifiableMap(releaseStatusCodeSetMap);
    }

    private void addTransitionRule(ReleaseStatusCode statusCode, List<ReleaseStatusCode> requiredPreviousStatusList) {
        Set<ReleaseStatusCode> requiredPreviousConditionSet = EnumSet.copyOf(requiredPreviousStatusList);
        RequiredPreviousCondition condition = RequiredPreviousCondition.builder()
                .requiredPreviosConditionSet(requiredPreviousConditionSet)
                .build();
        releaseStatusTransitionMap.put(statusCode, condition);
    }

    @Getter
    @Builder
    public static class RequiredPreviousCondition {
        Set<ReleaseStatusCode> requiredPreviosConditionSet;
    }
}
