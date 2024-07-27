package com.yeonieum.orderservice.web.controller;

import com.yeonieum.orderservice.domain.release.dto.ReleaseRequest;
import com.yeonieum.orderservice.domain.release.policy.ReleaseStatusPolicy;
import com.yeonieum.orderservice.domain.release.service.ReleaseService;
import com.yeonieum.orderservice.global.auth.Role;
import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.global.responses.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/release")
public class ReleaseController {

    private final ReleaseService releaseService;
    private final ReleaseStatusPolicy releaseStatusPolicy;

    @Operation(summary = "고객 출고 조회", description = "고객(seller)에게 접수된 출고리스트를 조회합니다. 출고상태에 따라 필터링이 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/release/list", method = "GET")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getCustomersRelease (@RequestParam Long customerId,
                                                            @RequestParam(required = false) ReleaseStatusCode releaseStatus,
                                                            @RequestParam(required = false, defaultValue = "0") int page,
                                                            @RequestParam(required = false, defaultValue = "10") int size
                                                            ){
        return new ResponseEntity<>(ApiResponse.builder()
                .result(releaseService.getReleaseDetailsByCustomerAndStatus(customerId, releaseStatus, PageRequest.of(page, size)))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "배송시작일 설정", description = "고객이 상품의 배송시작 날짜를 설정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "배송 시작일 설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "배송 시작일 설정 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/release/deliveryDate", method = "PATCH")
    @PatchMapping("/deliveryDate")
    public ResponseEntity<ApiResponse> changeDeliveryDate(@RequestBody ReleaseRequest.OfUpdateDeliveryDate updateDeliveryDate) {

        releaseService.changeDeliveryDate(updateDeliveryDate);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "출고상태 변경 요청", description = "고객이 상품의 출고상태 변경을 요청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "출고 상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "출고 상태 변경 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/release/status", method = "PATCH")
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse> changeReleaseStatus(@RequestBody ReleaseRequest.OfUpdateReleaseStatus updateStatus) {
        String Role = "CUSTOMER";
        if(!releaseStatusPolicy.getReleaseStatusPermission().get(updateStatus.getReleaseStatusCode()).contains(Role)) {
            throw new RuntimeException("접근권한이 없습니다.");
        }

        releaseService.changReleaseStatus(updateStatus);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "출고 메모 작성", description = "고객은 출고메모를 작성할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "출고 메모 작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "출고 메모 작성 실패")
    })
    @PatchMapping("/memo")
    public ResponseEntity<ApiResponse> changeReleaseMemo(@RequestBody ReleaseRequest.OfRegisterMemo updateRegisterMemo) {

        releaseService.changeReleaseMemo(updateRegisterMemo);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "출고 보류 사유 작성", description = "고객은 출고 보류 사유를 작성할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "출고 보류 사유 작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "출고 보류 사유 작성 실패")
    })
    @PatchMapping("/hold-memo")
    public ResponseEntity<ApiResponse> changeReleaseHoldMemo(@RequestBody ReleaseRequest.OfHoldMemo updateHoldMemo) {

        releaseService.changeReleaseHoldMemo(updateHoldMemo);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "출고상태 일괄 변경 요청", description = "고객이 상품의 출고상태를 일괄 변경 요청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "출고 상태 일괄 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "출고 상태 일괄 변경 실패")
    })
    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse> changeBulkReleaseStatus(@RequestBody ReleaseRequest.OfBulkUpdateReleaseStatus updateStatus) {
        String Role = "CUSTOMER";
        if(!releaseStatusPolicy.getReleaseStatusPermission().get(updateStatus.getReleaseStatusCode()).contains(Role)) {
            throw new RuntimeException("접근권한이 없습니다.");
        }

        releaseService.changeBulkReleaseStatus(updateStatus);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "합포장 요청", description = "고객이 상품의 합포장을 요청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "합포장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "합포장 실패")
    })
    @PatchMapping("/combined-packaging")
    public ResponseEntity<ApiResponse> changeReleaseStatus(@RequestBody ReleaseRequest.OfBulkUpdateReleaseStatus updateStatus) {
        String Role = "CUSTOMER";
        if(!releaseStatusPolicy.getReleaseStatusPermission().get(updateStatus.getReleaseStatusCode()).contains(Role)) {
            throw new RuntimeException("접근권한이 없습니다.");
        }

        releaseService.changeCombinedPackaging(updateStatus);
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "고객 출고 상태별 카운팅 조회", description = "고객(seller)에게 접수된 상품들의 출고 상태별 카운팅 수 조회 기능입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "출고 상태별 카운팅 수 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "출고 상태별 카운팅 수 조회 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/release/status/counts", method = "GET")
    @GetMapping("/status/counts")
    public ResponseEntity<ApiResponse> countReleaseStatus (@RequestParam Long customerId){
        return new ResponseEntity<>(ApiResponse.builder()
                .result(releaseService.countReleaseStatus(customerId))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }
}
