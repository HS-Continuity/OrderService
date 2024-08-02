package com.yeonieum.orderservice.domain.release.dto;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class ReleaseRequest {

    @Getter
    @NoArgsConstructor
    public static class OfUpdateReleaseStatus {
        String orderId;
        ReleaseStatusCode releaseStatusCode;
    }

    @Getter
    @NoArgsConstructor
    public static class OfBulkUpdateReleaseStatus  {
        List<String> orderIds;
        ReleaseStatusCode releaseStatusCode;
    }

    @Getter
    @NoArgsConstructor
    public static class OfUpdateDeliveryDate {
        String orderId;
        LocalDate startDeliveryDate;
    }

    @Getter
    @NoArgsConstructor
    public static class OfRegisterMemo {
        String orderId;
        String memo;
    }

    @Getter
    @NoArgsConstructor
    public static class OfHoldMemo{
        String orderId;
        String memo;
    }
}
