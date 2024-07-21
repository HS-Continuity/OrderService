package com.yeonieum.orderservice.web.controller;

import com.yeonieum.orderservice.domain.regularorder.dto.request.RegularOrderRequest;
import com.yeonieum.orderservice.domain.regularorder.service.RegularOrderService;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.global.responses.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @Operation(summary = "정기주문 구독", description = "회원이 요청한 정기주문을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "정기주문 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping
    public ResponseEntity<ApiResponse> subscriptionRegularDelivery(@RequestBody  RegularOrderRequest.OfCreation creationRequest) {
        regularOrderService.subscriptionDelivery(creationRequest);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.INSERT_SUCCESS)
                .build(), HttpStatus.CREATED);
    }

    @Operation(summary = "정기주문 내역 조회", description = "회원의 정기주문신청 내역을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문내역 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> retrieveRegularOrderList(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        String memberId = "memberId"; // TODO: 로그인한 사용자의 ID를 컨텍스트에서 가져와야 함
        Pageable pageable = PageRequest.of(page, size);

        return new ResponseEntity<>(ApiResponse.builder()
                .result(regularOrderService.retrieveRegularDeliveryList(memberId, pageable))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "정기주문 해지", description = "회원의 정기주문신청을 해지합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문신청 해지 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PutMapping("/cancel")
    public ResponseEntity<ApiResponse> cancelRegularOrder(@RequestParam(name = "regularOrderId") Long regularDeliveryApplicationId) {
        regularOrderService.cancelRegularDelivery(regularDeliveryApplicationId);
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
    @PutMapping("/{regularOrderId}/postpone")
    public ResponseEntity<ApiResponse> postponeRegularOrder(@RequestParam(name = "regularOrderId") Long regularDeliveryApplicationId,
                                                            @RequestBody RegularOrderRequest.OfPostPone postPoneRequest) {
        regularOrderService.skipRegularDeliveryReservation(regularDeliveryApplicationId, postPoneRequest);
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
        return new ResponseEntity<>(ApiResponse.builder()
                .result(regularOrderService.retrieveRegularDeliveryDetails(regularDeliveryApplicationId))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "고객에게 접수된 정기주문 월별 조회", description = "고객에게 접수된 정기주문을 월별로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정기주문월별 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/count")
    public ResponseEntity<ApiResponse> retrieveRegularOrderCountsBetweenMonth(@RequestParam LocalDate startDate,
                                                                              @RequestParam LocalDate endDate) {

        Long customerId = 1L; // TODO: 로그인한 사용자의 ID를 컨텍스트에서 가져와야 함
        return new ResponseEntity<>(ApiResponse.builder()
                .result(regularOrderService.retrieveRegularOrderCountsBetween(startDate, endDate, customerId))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }
}
