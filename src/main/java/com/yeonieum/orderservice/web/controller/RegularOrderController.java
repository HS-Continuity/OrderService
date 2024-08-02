package com.yeonieum.orderservice.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yeonieum.orderservice.domain.regularorder.dto.request.RegularOrderRequest;
import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;
import com.yeonieum.orderservice.domain.regularorder.service.RegularOrderService;
import com.yeonieum.orderservice.global.auth.Role;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.global.responses.code.SuccessCode;
import com.yeonieum.orderservice.global.usercontext.UserContextHolder;
import com.yeonieum.orderservice.infrastructure.messaging.service.OrderEventProduceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.yeonieum.orderservice.infrastructure.messaging.producer.OrderEventProducer.REGULAR_TOPIC;

/**
 * 1. 정기주문 생성
 * 2. 정기주문 내역 조회
 * 3. 정기주문회차 미루기(취소)
 * 4. 정기주문 상세 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regular-order")
public class RegularOrderController {

    private final RegularOrderService regularOrderService;
    private final OrderEventProduceService orderEventProduceService;

    @Operation(summary = "정기주문 구독", description = "회원이 요청한 정기주문을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "정기주문 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_CUSTOMER", "ROLE_MEMBER"}, url = "/api/regular-order", method = "POST")
    @PostMapping
    public ResponseEntity<ApiResponse> subscriptionRegularDelivery(@RequestBody  RegularOrderRequest.OfCreation creationRequest) throws JsonProcessingException {
        String member = UserContextHolder.getContext().getUserId();
        RegularOrderResponse.OfSuccess successResult = regularOrderService.subscriptionDelivery(member, creationRequest);
        orderEventProduceService.produceRegularOrderEvent(
                successResult.getMemberId(),
                successResult.getCustomerId(),
                successResult.getRegularDeliveryApplicationId(),
                REGULAR_TOPIC,
                "APPLY"
        );
        return new ResponseEntity<>(ApiResponse.builder()
                .result(successResult)
                .successCode(SuccessCode.INSERT_SUCCESS)
                .build(), HttpStatus.CREATED);
    }

    @Operation(summary = "정기주문 내역 조회", description = "회원의 정기주문신청 내역을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문내역 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_MEMBER"}, url = "/api/regular-order/list", method = "GET")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> retrieveRegularOrderList(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(required = false)LocalDate startDate,
                                                                @RequestParam(required = false)LocalDate endDate) {
        String member = UserContextHolder.getContext().getUserId();
        Pageable pageable = PageRequest.of(page, size);

        return new ResponseEntity<>(ApiResponse.builder()
                .result(regularOrderService.retrieveRegularDeliveryList(member, startDate, endDate, pageable))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "정기주문 해지", description = "회원의 정기주문신청을 해지합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문신청 해지 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_MEMBER"}, url = "/api/regular-order/cancel", method = "PUT")
    @PutMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelRegularOrder(@RequestParam(name = "regularOrderId") Long regularDeliveryApplicationId) throws JsonProcessingException {
        String member = UserContextHolder.getContext().getUserId();
        regularOrderService.cancelRegularDelivery(member, regularDeliveryApplicationId);
        orderEventProduceService.produceRegularOrderEvent(
                member,
                -1L,
                regularDeliveryApplicationId,
                REGULAR_TOPIC,
                "CANCEL"
        );

        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "정기주문 회차 미루기", description = "회원의 정기주문 해당회차를 미룹니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문 연기 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_MEMBER"}, url = "/api/regular-order/{regularOrderId}/postpone", method = "PUT")
    @PutMapping("/{regularDeliveryApplicationId}/postpone")
    public ResponseEntity<ApiResponse> postponeRegularOrder(@PathVariable Long regularDeliveryApplicationId) throws JsonProcessingException {
        String member = UserContextHolder.getContext().getUserId();
        regularOrderService.skipRegularDeliveryReservation(member, regularDeliveryApplicationId);
        orderEventProduceService.produceRegularOrderEvent(
                member,
                -1L,
                regularDeliveryApplicationId,
                REGULAR_TOPIC,
                "POSTPONE"
        );


        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "정기주문신청 내역 상세 조회", description = "회원의 정기주문 신청내역 상세를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/{regularOrderId}/detail")
    public ResponseEntity<ApiResponse> retrieveRegularOrderDetail(@PathVariable(name = "regularOrderId") Long regularDeliveryApplicationId) {
        String member = UserContextHolder.getContext().getUserId();
        return new ResponseEntity<>(ApiResponse.builder()
                .result(regularOrderService.retrieveRegularDeliveryDetails(member, regularDeliveryApplicationId))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "고객에게 접수된 정기주문 월별 조회", description = "고객에게 접수된 정기주문을 월별로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문월별 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse> retrieveRegularOrderCountsBetweenMonth(@RequestParam LocalDate startDate,
                                                                              @RequestParam LocalDate endDate) {

        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());
        return new ResponseEntity<>(ApiResponse.builder()
                .result(regularOrderService.retrieveRegularOrderSummaries(startDate, endDate, customer))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "고객에게 접수된 정기주문 일별 조회", description = "고객에게 접수된 정기주문을 일별로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문일별 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse> retrieveRegularOrderCountsBetweenDay(@RequestParam LocalDate date,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(defaultValue = "0") int page) {

        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());
        Pageable pageable = PageRequest.of(page, size);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(regularOrderService.retrieveRegularOrderList(date, customer, pageable))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }
}
