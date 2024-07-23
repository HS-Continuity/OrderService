package com.yeonieum.orderservice.infrastructure.feignclient;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.global.config.FeignConfig;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "memberservice", url = "http://localhost:8010", configuration = FeignConfig.class)
public interface MemberServiceFeignClient {

    @GetMapping("/api/member/order")
    ResponseEntity<ApiResponse<OrderResponse.MemberInfo>> getOrderMemberInfo(@RequestParam String memberId);
}
