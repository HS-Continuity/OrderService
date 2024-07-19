package com.yeonieum.orderservice.domain.productstock.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class StockUsageResponse {
    @Getter
    @Builder
    public static class AvailableStockDto {
        String orderId;
        Long productId;
        int quantity;
        Boolean isAvailableOrder;
    }


    @Builder
    @Getter
    public static class AvailableResponseList{
        List<AvailableStockDto> availableProductInventoryResponseList;
    }
}
