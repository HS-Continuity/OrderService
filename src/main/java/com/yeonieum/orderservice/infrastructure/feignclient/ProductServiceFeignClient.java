package com.yeonieum.orderservice.infrastructure.feignclient;

import com.yeonieum.orderservice.domain.productstock.request.StockUsageRequest;
import com.yeonieum.orderservice.domain.productstock.response.StockUsageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "productservice", url = "http://localhost:8020")
public interface ProductServiceFeignClient {
    @PostMapping("/api/inventory/")
    StockUsageResponse.AvailableResponseList checkAvailableOrderProduct(@RequestBody StockUsageRequest.IncreaseStockUsageList increaseStockUsageDtoList);
}