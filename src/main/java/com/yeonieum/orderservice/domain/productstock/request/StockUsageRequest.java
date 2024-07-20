package com.yeonieum.orderservice.domain.productstock.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class StockUsageRequest {
    @Getter
    @Builder
    public static class OfIncreasing {
        String orderId;
        Long productId;
        int quantity;
    }

    @Getter
    @Builder
    public static class IncreaseStockUsageList {
        List<OfIncreasing> ofIncreasingList;
    }
}
