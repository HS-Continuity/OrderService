package com.yeonieum.orderservice.domain.release.dto;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReleaseRequest {

    @Getter
    @NoArgsConstructor
    public static class OfUpdateReleaseStatus {
        String orderId;
        ReleaseStatusCode releaseStatusCode;
    }
}
