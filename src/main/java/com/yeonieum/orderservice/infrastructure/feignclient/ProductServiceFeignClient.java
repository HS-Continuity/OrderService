package com.yeonieum.orderservice.infrastructure.feignclient;

import com.yeonieum.orderservice.domain.productstock.request.StockUsageRequest;
import com.yeonieum.orderservice.domain.productstock.response.StockUsageResponse;
import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "productservice", url = "http://localhost:8020")
public interface ProductServiceFeignClient {
    @PostMapping("/api/inventory/stock-usage")
    StockUsageResponse.AvailableResponseList checkAvailableOrderProduct(@RequestBody StockUsageRequest.IncreaseStockUsageList increaseStockUsageDtoList);

    @GetMapping("/api/management/{productId}")
    ResponseEntity<ApiResponse<RegularOrderResponse.ProductOrder>> retrieveProductInformation(@PathVariable Long productId);

    @GetMapping("/api/management/products")
    ResponseEntity<ApiResponse<Map<Long, RegularOrderResponse.ProductOrder>>> bulkRetrieveProductInformation(@RequestParam List<Long> productIdList);

    @GetMapping("/api/customer/delivery-fee/{customerId}")
    ResponseEntity<ApiResponse<Integer>> retrieveDeliveryFee(@PathVariable Long customerId);
}

