package com.yeonieum.orderservice.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yeonieum.orderservice.domain.notification.service.OrderNotificationServiceForCustomer;
import com.yeonieum.orderservice.domain.order.dto.request.OrderRequest;
import com.yeonieum.orderservice.domain.order.policy.OrderStatusPolicy;
import com.yeonieum.orderservice.domain.order.service.OrderProcessService;
import com.yeonieum.orderservice.domain.order.service.OrderTrackingService;
import com.yeonieum.orderservice.global.auth.Role;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.global.responses.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderDetailController {

    private final OrderTrackingService orderTrackingService;
    private final OrderProcessService orderProcessService;
    private final OrderNotificationServiceForCustomer notificationService;
    private final OrderStatusPolicy orderStatusPolicy;

    @Operation(summary = "고객용 주문 조회", description = "고객(seller)에게 접수된 주문리스트를 조회합니다. 주문상태에 따라 필터링이 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/order/customer-service", method = "GET")
    @GetMapping("/customer-service")
    public ResponseEntity<ApiResponse> getCustomersOrder (@RequestParam Long customerId,
                                                          @RequestParam(required = false) OrderStatusCode orderStatus,
                                                          @RequestParam(required = false, defaultValue = "0") int page,
                                                          @RequestParam(required = false, defaultValue = "10") int size){
        return new ResponseEntity<>(ApiResponse.builder()
                .result(orderTrackingService.retrieveOrdersForCustomer(customerId, orderStatus, PageRequest.of(page, size)))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "주문상태별 총 주문 수 조회", description = "고객(seller)에게 접수된 주문중 주문상태별 주문접수 수(count)를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/order/counts", method = "GET")
    @GetMapping("/counts")
    public ResponseEntity<ApiResponse> getCustomersOrderCounts (@RequestParam Long customerId,
                                                                @RequestParam OrderStatusCode orderStatus){
        return new ResponseEntity<>(ApiResponse.builder()
                .result(orderTrackingService.retrieveTotalOrderCountForCustomer(customerId, orderStatus))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "회원의 주문내역 리스트 조회", description = "회원의 주문내역리스트를 조회합니다. 기간별 조회는 필수입니다.(3개월 권장)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_MEMBER"}, url = "/api/order/member-service", method = "GET")
    @GetMapping("/member-service")
    public ResponseEntity<ApiResponse> getMembersOrder (@RequestParam String memberId,
                                                        @RequestParam LocalDate startDate,
                                                        @RequestParam LocalDate endDate,
                                                        @RequestParam(required = false, defaultValue = "0") int page,
                                                        @RequestParam(required = false, defaultValue = "10") int size){
        return new ResponseEntity<>(ApiResponse.builder()
                .result(orderTrackingService.retrieveOrderForMember(memberId,startDate,endDate,PageRequest.of(page, size)))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "주문상품 상태변경 요청", description = "회원과 고객이 주문상품상태 변경을 요청합니다.[주의: 주문전체 주문서에 대한 상태변경과 혼동하지 않도록 주의]")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문상태변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_CUSTOMER", "ROLE_MEMBER"}, url = "/api/order/product/status", method = "PATCH")
    @PatchMapping("/product/status")
    public ResponseEntity<ApiResponse> changeProductOrder(@RequestBody OrderRequest.OfUpdateProductOrderStatus updateProductOrderStatus) {
        orderProcessService.changeOrderProductStatus(updateProductOrderStatus);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "주문서 상태변경 요청", description = "회원과 고객이 주문상태 변경을 요청합니다.[주의: 주문상품에 대한 상태변경이 아닌 전체 주문서에 대한 상태변경]")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문상태변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_MEMBER", "ROLE_CUSTOMER"}, url = "/api/order/status", method = "PATCH")
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse> changeOrderStatus(@RequestBody OrderRequest.OfUpdateOrderStatus updateStatus) {
        String Role = "컨텍스트에서 가져올 예정";
        if(!orderStatusPolicy.getOrderStatusPermission().get(updateStatus.getOrderStatusCode().getCode()).equals(Role)) {
            throw new RuntimeException("접근권한이 없습니다.");
        }

        orderProcessService.changeOrderStatus(updateStatus);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "주문 생성", description = "주문을 신청하고 생성하는 기능입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "회원 쿠폰 조회 실패")
    })
    @Role(role = {"ROLE_MEMBER", "ROLE_CUSTOMER"}, url = "/api/order", method = "POST")
    @PostMapping
    public ResponseEntity<ApiResponse> placeOrder (@RequestBody OrderRequest.OfCreation creationRequest) throws JsonProcessingException {
        String memberId = "컨텍스트에서 가져올 예정";
        orderProcessService.placeOrder(creationRequest, memberId);
        notificationService.sendEventMessage(creationRequest.getCustomerId());

        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "주문상태 일괄 변경 요청", description = "고객이 상품의 주문상태를 일괄 변경 요청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 상태 일괄 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "주문 상태 일괄 변경 실패")
    })
    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse> changeBulkOrderStatus(@RequestBody OrderRequest.OfBulkUpdateOrderStatus updateStatus) {
        String Role = "CUSTOMER";
        if(!orderStatusPolicy.getOrderStatusPermission().get(updateStatus.getOrderStatusCode()).contains(Role)) {
            throw new RuntimeException("접근권한이 없습니다.");
        }

        orderProcessService.changeBulkOrderStatus(updateStatus);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }
}
