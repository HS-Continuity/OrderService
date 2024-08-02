package com.yeonieum.orderservice.domain.productstock.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class StockUsageResponse {
    @Getter
    @NoArgsConstructor
    public static class AvailableStockDto {
        String orderDetailId;
        Long productId;
        int quantity;
        Boolean isAvailableOrder;
    }


    @Getter
    @NoArgsConstructor
    public static class AvailableResponseList{
        List<AvailableStockDto> availableProductInventoryResponseList;
    }
}
