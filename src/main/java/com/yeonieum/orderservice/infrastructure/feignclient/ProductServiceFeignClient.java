package com.yeonieum.orderservice.infrastructure.feignclient;

import com.yeonieum.orderservice.domain.productstock.request.StockUsageRequest;
import com.yeonieum.orderservice.domain.productstock.response.StockUsageResponse;
import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;
import com.yeonieum.orderservice.global.config.FeignConfig;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.OfOrderInformation;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.RetrieveOrderInformationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "productservice", configuration = FeignConfig.class)
public interface ProductServiceFeignClient {
    @PostMapping("/productservice/api/inventory/stock-usage")
    ResponseEntity<StockUsageResponse.AvailableResponseList> checkAvailableOrderProduct(@RequestBody StockUsageRequest.IncreaseStockUsageList increaseStockUsageDtoList);

    @GetMapping("/productservice/api/management/{productId}")
    ResponseEntity<ApiResponse<RegularOrderResponse.ProductOrder>> retrieveProductInformation(@PathVariable("productId") Long productId);

    @GetMapping("/productservice/api/management/products")
    ResponseEntity<ApiResponse<Map<Long, RegularOrderResponse.ProductOrder>>> bulkRetrieveProductInformation(@RequestParam List<Long> productIdList);

    @GetMapping("/productservice/api/shopping/product/order/{productIdList}")
    public ResponseEntity<ApiResponse<RetrieveOrderInformationResponse>> retrieveOrderProductInformation(@RequestParam("productIdList") Long productIdList);
    
    @GetMapping("/productservice/api/shopping/product/orders")
    ResponseEntity<ApiResponse<Set<RetrieveOrderInformationResponse>>> retrieveOrderProductInformation(@RequestParam("productIdList") List<Long> productIdList);

    @GetMapping("/productservice/api/customer/delivery-fee/{customerId}")
    ResponseEntity<ApiResponse<Integer>> retrieveDeliveryFee(@PathVariable("customerId") Long customerId);

}

