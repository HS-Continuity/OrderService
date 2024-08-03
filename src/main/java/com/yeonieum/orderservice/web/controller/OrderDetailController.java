package com.yeonieum.orderservice.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yeonieum.orderservice.domain.notification.service.OrderNotificationServiceForCustomer;
import com.yeonieum.orderservice.domain.order.dto.request.OrderRequest;
import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.order.policy.OrderStatusPolicy;
import com.yeonieum.orderservice.domain.order.service.OrderProcessService;
import com.yeonieum.orderservice.domain.order.service.OrderTrackingService;
import com.yeonieum.orderservice.domain.statistics.service.StatisticsService;
import com.yeonieum.orderservice.global.auth.Role;
import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import com.yeonieum.orderservice.global.enums.OrderType;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.global.responses.code.SuccessCode;
import com.yeonieum.orderservice.global.usercontext.UserContextHolder;
import com.yeonieum.orderservice.infrastructure.messaging.dto.ShippedEventMessage;
import com.yeonieum.orderservice.infrastructure.messaging.producer.OrderEventProducer;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.yeonieum.orderservice.infrastructure.messaging.producer.OrderEventProducer.ORDER_TOPIC;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderDetailController {

    private final OrderTrackingService orderTrackingService;
    private final OrderProcessService orderProcessService;
    private final OrderNotificationServiceForCustomer notificationService;
    private final OrderStatusPolicy orderStatusPolicy;
    private final OrderEventProduceService orderEventProduceService;
    private final StatisticsService statisticsService;
    private final OrderEventProducer orderEventProducer;

    @Operation(summary = "고객용 주문 조회", description = "고객(seller)에게 접수된 주문리스트를 조회합니다. 주문상태에 따라 필터링이 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/order/customer-service", method = "GET")
    @GetMapping("/customer-service")
    public ResponseEntity<ApiResponse> getCustomersOrder (@RequestParam Long customerId,
                                                          @RequestParam(required = false) OrderStatusCode orderStatusCode,
                                                          @RequestParam(required = false) String orderDetailId,
                                                          @RequestParam(required = false) LocalDateTime orderDateTime,
                                                          @RequestParam(required = false) String recipient,
                                                          @RequestParam(required = false) String recipientPhoneNumber,
                                                          @RequestParam(required = false) String recipientAddress,
                                                          @RequestParam(required = false) String memberId,
                                                          @RequestParam(required = false) String memberName,
                                                          @RequestParam(required = false) String memberPhoneNumber,
                                                          @RequestParam(required = false) LocalDate startDate,
                                                          @RequestParam(required = false) LocalDate endDate,
                                                          @RequestParam(required = false, defaultValue = "0") int page,
                                                          @RequestParam(required = false, defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());
        return new ResponseEntity<>(ApiResponse.builder()
                .result(orderTrackingService.retrieveOrdersForCustomer(customer, orderStatusCode, orderDetailId, orderDateTime, recipient, recipientPhoneNumber, recipientAddress, memberId, memberName, memberPhoneNumber, startDate, endDate, pageable))
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
        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());
        return new ResponseEntity<>(ApiResponse.builder()
                .result(orderTrackingService.retrieveTotalOrderCountForCustomer(customer, orderStatus))
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
    public ResponseEntity<ApiResponse> getMembersOrder (@RequestParam LocalDate startDate,
                                                        @RequestParam LocalDate endDate,
                                                        @RequestParam(required = false, defaultValue = "0") int page,
                                                        @RequestParam(required = false, defaultValue = "10") int size){
        String member = UserContextHolder.getContext().getUserId();
        return new ResponseEntity<>(ApiResponse.builder()
                .result(orderTrackingService.retrieveOrderForMember(member,startDate,endDate,PageRequest.of(page, size)))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "회원용 주문 상세 조회", description = "회원용 주문 상세정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_MEMBER", "ROLE_CUSTOMER"}, url = "/api/order/member-service/{orderDetailId}", method = "GET")
    @GetMapping("/member-service/{orderDetailId}")
    public ResponseEntity<ApiResponse> getOrderDetail (@PathVariable String orderDetailId) {
        String member = UserContextHolder.getContext().getUserId();
        return new ResponseEntity<>(ApiResponse.builder()
                .result(orderTrackingService.retrieveOrderDetailForMember(member, orderDetailId))
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
    public ResponseEntity<ApiResponse> changeProductOrder(@RequestBody OrderRequest.OfUpdateProductOrderStatus updateProductOrderStatus) throws JsonProcessingException {
        String loginId;
        String roleType = UserContextHolder.getContext().getRoleType();

        if(UserContextHolder.getContext().getRoleType().equals("ROLE_MEMBER")) {
            loginId = UserContextHolder.getContext().getUserId();
        } else {
            loginId = UserContextHolder.getContext().getUniqueId();
        }

        OrderResponse.OfResultUpdateStatus result = orderProcessService.changeOrderProductStatus(roleType, loginId, updateProductOrderStatus);
        if(updateProductOrderStatus.getOrderStatusCode().equals(OrderStatusCode.CANCELED)) {
            orderEventProducer.sendCancelMessage(
                    result.getProductOrderEntityList().stream().map(productOrderEntity ->
                            ShippedEventMessage.convertedBy(result.getOrderDetailId(), productOrderEntity)).collect(Collectors.toList()));

        }

        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "주문서 상태변경 요청", description = "회원과 고객이 주문상태 변경을 요청합니다.[주의: 주문상품에 대한 상태변경이 아닌 전체 주문서에 대한 상태변경]")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문상태변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Role(role = {"ROLE_MEMBER", "ROLE_CUSTOMER"}, url = "/api/order/status", method = "PATCH")
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse> changeOrderStatus(@RequestBody OrderRequest.OfUpdateOrderStatus updateStatus) throws JsonProcessingException {
        String loginId;
        String roleType = UserContextHolder.getContext().getRoleType();

        if(UserContextHolder.getContext().getRoleType().equals("ROLE_MEMBER")) {
            loginId = UserContextHolder.getContext().getUserId();
        } else {
            loginId = UserContextHolder.getContext().getUniqueId();
        }
        if(!orderStatusPolicy.getOrderStatusPermission().get(updateStatus.getOrderStatusCode()).contains(roleType)) {
            throw new RuntimeException("접근권한이 없습니다.");
        }

        OrderResponse.OfResultUpdateStatus result = orderProcessService.changeOrderStatus(roleType, loginId, updateStatus);
        if(updateStatus.getOrderStatusCode().equals(OrderStatusCode.PREPARING_PRODUCT)) {
            // ShippedEventMessage의 리스트 컬렉션 생성
            orderEventProducer.sendApproveMessage(
                    result.getProductOrderEntityList().stream().map(productOrderEntity ->
                                    ShippedEventMessage.convertedBy(result.getOrderDetailId(), productOrderEntity)).collect(Collectors.toList()));
        }

        if(updateStatus.getOrderStatusCode().equals(OrderStatusCode.CANCELED)) {
            orderEventProduceService.produceOrderEvent(
                    loginId,
                    -1L,
                    updateStatus.getOrderId(),
                    ORDER_TOPIC ,
                    "CANCELED"
            );

            orderEventProducer.sendCancelMessage(
                    result.getProductOrderEntityList().stream().map(productOrderEntity ->
                            ShippedEventMessage.convertedBy(result.getOrderDetailId(), productOrderEntity)).collect(Collectors.toList()));

        }
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
        String member = UserContextHolder.getContext().getUserId();
        OrderResponse.OfResultPlaceOrder resultPlaceOrder = orderProcessService.placeOrder(creationRequest, member);
        String orderDetailId = resultPlaceOrder.getOrderDetailId();

        // 주문 성공 시 SSE 알림 및 이벤트 발행
        if(resultPlaceOrder.isPayment()) {
            notificationService.sendEventMessage(creationRequest.getCustomerId());
            orderEventProduceService.produceOrderEvent(
                    member,
                    resultPlaceOrder.getCustomerId(),
                    orderDetailId,
                    ORDER_TOPIC ,
                    "PAYMENT_COMPLETED"
            );
        } else {
            throw new RuntimeException("주문 생성 실패");
        }

        return new ResponseEntity<>(ApiResponse.builder()
                .result(resultPlaceOrder)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "주문상태 일괄 변경 요청", description = "고객이 상품의 주문상태를 일괄 변경 요청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 상태 일괄 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "주문 상태 일괄 변경 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/order/bulk-status", method = "PATCH")
    @PatchMapping("/bulk-status")
    public ResponseEntity<ApiResponse> changeBulkOrderStatus(@RequestBody OrderRequest.OfBulkUpdateOrderStatus updateStatus) throws JsonProcessingException {
        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());
        String roleType = UserContextHolder.getContext().getRoleType();

        if(!orderStatusPolicy.getOrderStatusPermission().get(updateStatus.getOrderStatusCode()).contains(roleType)) {
            throw new RuntimeException("접근권한이 없습니다.");
        }

        List<OrderResponse.OfResultUpdateStatus> resultPlaceOrders = orderProcessService.changeBulkOrderStatus(customer, updateStatus);
        for (OrderResponse.OfResultUpdateStatus resultPlaceOrder : resultPlaceOrders) {
            if(updateStatus.getOrderStatusCode().equals(OrderStatusCode.PREPARING_PRODUCT)) {
                orderEventProducer.sendApproveMessage(
                        resultPlaceOrder.getProductOrderEntityList().stream().map(productOrderEntity ->
                                ShippedEventMessage.convertedBy(resultPlaceOrder.getOrderDetailId(), productOrderEntity)).collect(Collectors.toList()));
            }

            if(updateStatus.getOrderStatusCode().equals(OrderStatusCode.CANCELED)) {
                orderEventProducer.sendCancelMessage(
                        resultPlaceOrder.getProductOrderEntityList().stream().map(productOrderEntity ->
                                ShippedEventMessage.convertedBy(resultPlaceOrder.getOrderDetailId(), productOrderEntity)).collect(Collectors.toList()));
            }
        }
        return new ResponseEntity<>(ApiResponse.builder()
                .result(null)
                .successCode(SuccessCode.UPDATE_SUCCESS)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "회원 판매상품 성별 top3 조회", description = "회원의 판매상품 중 성별로 top3 상품을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "상품 조회 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/order/ranking/gender", method = "GET")
    @GetMapping("/ranking/gender")
    public ResponseEntity<ApiResponse> getOrderGenderTop3 (@RequestParam Long customerId, @RequestParam Gender gender) {
        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());

        return new ResponseEntity<>(ApiResponse.builder()
                .result(statisticsService.genderProductOrderCounts(customer, gender))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "회원 판매상품 연령별 top3 조회", description = "회원의 판매상품 중 연령별로 top3 상품을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "상품 조회 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/order/ranking/age-range", method = "GET")
    @GetMapping("/ranking/age-range")
    public ResponseEntity<ApiResponse> getOrderAgeRangeTop3 (@RequestParam Long customerId, @RequestParam int ageRange) {
        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());

        return new ResponseEntity<>(ApiResponse.builder()
                .result(statisticsService.ageProductOrderCounts(customer, ageRange))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }

    @Operation(summary = "회원 판매타입별 상품 판매량 조회", description = "회원의 판매상품별 상품을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "상품 조회 실패")
    })
    @Role(role = {"ROLE_CUSTOMER"}, url = "/api/order/ranking/order-type", method = "GET")
    @GetMapping("/ranking/order-type")
    public ResponseEntity<ApiResponse> getOrderAgeRangeTop3 (@RequestParam Long customerId, @RequestParam OrderType orderType) {
        Long customer = Long.valueOf(UserContextHolder.getContext().getUniqueId());

        return new ResponseEntity<>(ApiResponse.builder()
                .result(statisticsService.orderTypeProductOrderCounts(customer, orderType))
                .successCode(SuccessCode.SELECT_SUCCESS)
                .build(), HttpStatus.OK);
    }
}
