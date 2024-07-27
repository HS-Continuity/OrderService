package com.yeonieum.orderservice.web.controller;

import com.yeonieum.orderservice.domain.delivery.service.DeliveryService;
import com.yeonieum.orderservice.global.auth.Role;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.global.responses.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "고객 배송 조회", description = "고객(seller)에게 접수된 배송리스트를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "배송 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "배송 조회 실패")
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getCustomersDelivery (@RequestParam Long customerId){
        return new ResponseEntity<>(ApiResponse.builder()
                .result(deliveryService.retrieveDeliveryList(customerId))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "고객 배송 상태별 카운팅 조회", description = "고객(seller)에게 접수된 상품들의 배송 상태별 카운팅 수 조회 기능입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "배송 상태별 카운팅 수 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "배송 상태별 카운팅 수 조회 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/delivery/status/counts", method = "GET")
    @GetMapping("/status/counts")
    public ResponseEntity<ApiResponse> countDeliveryStatus (@RequestParam Long customerId){
        return new ResponseEntity<>(ApiResponse.builder()
                .result(deliveryService.countDeliveryStatus(customerId))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }
}
