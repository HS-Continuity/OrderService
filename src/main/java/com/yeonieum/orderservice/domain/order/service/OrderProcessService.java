package com.yeonieum.orderservice.domain.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yeonieum.orderservice.domain.order.dto.request.OrderRequest;
import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.OrderStatus;
import com.yeonieum.orderservice.domain.order.entity.ProductOrderEntity;
import com.yeonieum.orderservice.domain.order.exception.OrderException;
import com.yeonieum.orderservice.domain.order.policy.OrderStatusPolicy;
import com.yeonieum.orderservice.domain.order.repository.OrderDetailRepository;
import com.yeonieum.orderservice.domain.order.repository.OrderStatusRepository;
import com.yeonieum.orderservice.domain.order.repository.PaymentInformationRepository;
import com.yeonieum.orderservice.domain.productstock.request.StockUsageRequest;
import com.yeonieum.orderservice.domain.productstock.response.StockUsageResponse;
import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.domain.release.entity.ReleaseStatus;
import com.yeonieum.orderservice.domain.release.repository.ReleaseRepository;
import com.yeonieum.orderservice.domain.release.repository.ReleaseStatusRepository;
import com.yeonieum.orderservice.domain.statistics.entity.Statistics;
import com.yeonieum.orderservice.domain.statistics.repository.StatisticsRepository;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import com.yeonieum.orderservice.global.enums.OrderType;
import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import com.yeonieum.orderservice.infrastructure.feignclient.MemberServiceFeignClient;
import com.yeonieum.orderservice.infrastructure.feignclient.ProductServiceFeignClient;
import com.yeonieum.orderservice.infrastructure.messaging.producer.OrderEventProducer;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yeonieum.orderservice.domain.order.exception.OrderExceptionCode.*;


@Service
@RequiredArgsConstructor
public class OrderProcessService {
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentInformationRepository paymentInformationRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseStatusRepository releaseStatusRepository;
    private final ProductServiceFeignClient stockFeignClient;
    private final MemberServiceFeignClient memberServiceFeignClient;
    private final OrderStatusPolicy orderStatusPolicy;
    private final OrderEventProducer orderEventProducer;
    private static final String CANCELLED_PAYMENT_AMOUNT = "cancelledPaymentAmount";
    private static final String CANCELLED_DISCOUNT_AMOUNT = "cancelledDiscountAmount";
    private static final String CANCELLED_ORIGIN_PRODUCT_PRICE = "cancelledOriginProductPrice";
    private final StatisticsRepository statisticsRepository;


    /**
     * 전체 주문상태 수정 서비스[상품주문에 대한 주문상태 변경은 일어나지 않음]
     * (수정 권한 체크는 컨트롤러에서 진행)
     *
     * @param updateStatus
     * @return
     */
    @Transactional
    public OrderResponse.OfResultUpdateStatus changeOrderStatus(String roleType, String loginId, OrderRequest.OfUpdateOrderStatus updateStatus) {
        OrderDetail orderDetail = orderDetailRepository.findById(updateStatus.getOrderId()).orElseThrow(
                () ->  new OrderException(ORDER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if(roleType.equals("ROLE_MEMBER")) {
            if(!orderDetail.getMemberId().equals(loginId)) {
                throw new IllegalArgumentException("접근 권한이 없습니다.");
            }
        } else {
            if(!loginId.equals(orderDetail.getCustomerId())) {
                throw new IllegalArgumentException("접근 권한이 없습니다.");
            }
        }

        OrderStatus requestedStatus = orderStatusRepository.findByStatusName(updateStatus.getOrderStatusCode());
        OrderStatusCode requestedStatusCode = requestedStatus.getStatusName();
        OrderStatusCode orderStatus = orderDetail.getOrderStatus().getStatusName();
        if (!orderStatusPolicy.getOrderStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(orderStatus)) {
            throw new OrderException(ORDER_STATUS_TRANSITION_RULE_VIOLATION, HttpStatus.CONFLICT);
        }

        switch (updateStatus.getOrderStatusCode()) {
            case AWAITING_RELEASE -> {
                ReleaseStatus releaseStatus = releaseStatusRepository.findByStatusName(ReleaseStatusCode.AWAITING_RELEASE);
                releaseRepository.save(Release.builder()
                        .orderDetail(orderDetail)
                        .releaseStatus(releaseStatus)
                        .build());
            }
            case PREPARING_PRODUCT, CANCELED, REFUND_REQUEST, REFUNDED -> orderDetail.changeOrderStatus(requestedStatus);
            default -> throw new OrderException(INVALID_ACCESS, HttpStatus.CONFLICT);
        }
        // 주문 상태 변경
        orderDetail.changeOrderStatus(requestedStatus);
        orderDetail.getOrderList().getProductOrderEntityList().forEach(productOrderEntity ->
                productOrderEntity.changeStatus(requestedStatusCode));
        // 명시적 저장
        orderDetailRepository.save(orderDetail);
        return OrderResponse.OfResultUpdateStatus.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .orderStatusCode(requestedStatusCode.getCode())
                .productOrderEntityList(orderDetail.getOrderList().getProductOrderEntityList().stream().filter(
                        productOrderEntity -> productOrderEntity.getStatus().equals(requestedStatusCode)).collect(Collectors.toList())
                ).build();
    }

    /**
     * 주문상품에 대한 주문상태 변경 서비스
     *
     * @param updateProductOrderStatus
     */
    @Transactional
    public OrderResponse.OfResultUpdateStatus changeOrderProductStatus(String roleType, String loginId, OrderRequest.OfUpdateProductOrderStatus updateProductOrderStatus) {
        OrderDetail orderDetail = orderDetailRepository.findById(updateProductOrderStatus.getOrderId())
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if(roleType.equals("ROLE_MEMBER")) {
            if(!orderDetail.getMemberId().equals(loginId)) {
                throw new IllegalArgumentException("접근 권한이 없습니다.");
            }
        } else {
            if(!loginId.equals(String.valueOf(orderDetail.getCustomerId()))) {
                throw new IllegalArgumentException("접근 권한이 없습니다.");
            }
        }
        // TODO : 상품 json 변경감지 할 수 있는지 테스트 예정 -> deepcopy 활용

        List<ProductOrderEntity> productOrderEntityList = orderDetail.getOrderList().getProductOrderEntityList();
        ProductOrderEntity productOrder = productOrderEntityList.stream().filter(productOrderEntity ->
                productOrderEntity.getProductId() == updateProductOrderStatus.getProductId()).findFirst().orElseThrow(
                () -> new OrderException(PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        OrderStatus requestedStatus = orderStatusRepository.findByStatusName(updateProductOrderStatus.getOrderStatusCode());
        OrderStatusCode requestedStatusCode = requestedStatus.getStatusName();
        OrderStatusCode productOrderStatus = productOrder.getStatus();
        if (!orderStatusPolicy.getOrderStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(productOrderStatus)) {
            throw new OrderException(ORDER_STATUS_TRANSITION_RULE_VIOLATION, HttpStatus.CONFLICT);
        }


        OrderStatusCode requestedCode = updateProductOrderStatus.getOrderStatusCode();
        switch (requestedCode) {
            case CANCELED, REFUND_REQUEST, REFUNDED -> {
                productOrder.changeStatus(requestedCode);
            }
            default -> throw new OrderException(INVALID_ACCESS, HttpStatus.BAD_REQUEST);
        }


        if (productOrderEntityList.stream().allMatch(productOrderEntity -> productOrderEntity.getStatus().equals(requestedCode))) {
            orderDetail.changeOrderStatus(orderStatusRepository.findByStatusName(requestedCode));
        }
        orderDetail.changeOrderList(orderDetail.getOrderList()); // deepcopy
        orderDetailRepository.save(orderDetail);

        return OrderResponse.OfResultUpdateStatus.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .productOrderEntityList(productOrderEntityList.stream().filter(
                        productOrderEntity -> productOrderEntity.getStatus().equals(requestedCode)).collect(Collectors.toList()
                ))
                .orderStatusCode(requestedCode.getCode())
                .build();
    }


    /**
     * 회원의 주문신청 서비스
     * 0. 주문요청을 받는다.
     * 1. 주문가능한 재고상태인지 상품서비스에 요청 & 주문불가 상품에 대해 주문취소 처리
     * 2. 주문서 생성
     * 3. 주문서에 대한 결제 요청 & 결제 완료 응답받으면 주문서 상태 변경
     * 4. 주문서 및 결제 정보 저장
     * 5. 주문생성 성공 이벤트 발행(카프카 인프라 구축 예정)
     *
     * @param orderCreation
     */
    @Transactional(rollbackFor = {RuntimeException.class})
    public OrderResponse.OfResultPlaceOrder placeOrder(OrderRequest.OfCreation orderCreation, String memberId) throws JsonProcessingException {
        if(orderCreation.getMemberCouponId() != null) {
            try {
                boolean result = memberServiceFeignClient.useMemberCouponStatus(orderCreation.getMemberCouponId()).getBody().getResult();
                if (!result) {
                    throw new OrderException(COUPON_ALREADY_USED, HttpStatus.CONFLICT);
                }
            } catch (FeignException e) {
                throw new OrderException(COUPON_USE_FAILED, HttpStatus.CONFLICT);
            }
        }

        String orderDetailId = makeOrderId();
        ResponseEntity<StockUsageResponse.AvailableResponseList> responses = null;
        boolean isAvailableProductService = true;
        try {
            responses = checkAvailableProductOrder(orderCreation, orderDetailId);
            isAvailableProductService = responses.getStatusCode().is2xxSuccessful() ? true : false;
        } catch (FeignException e) {
            e.printStackTrace();
            isAvailableProductService = false;
        }
        OrderStatus orderStatus = null;
        Map<String, Integer> paymentAmountMap = null;
        if (isAvailableProductService) {
            paymentAmountMap = updateProductOrder(orderCreation, responses.getBody());
            orderCreation.changePaymentAmount(orderCreation.getPaymentAmount() - paymentAmountMap.get(CANCELLED_PAYMENT_AMOUNT));
            orderStatus = orderStatusRepository.findByStatusName(OrderStatusCode.PENDING);
        } else {
            orderStatus = orderStatusRepository.findByStatusName(OrderStatusCode.CANCELED);
        }

        // 주문서 생성
        OrderDetail orderDetail = orderCreation.toOrderDetailEntity(memberId, orderStatus, orderDetailId);
        // 주문서에 대한 결제 요청 & 결제 완료 됐으면 주문서 상태 변경
        final boolean isPayment = isAvailableProductService ? checkPaymentValidation() : false;
        final OrderStatus pending = orderStatusRepository.findByStatusName(OrderStatusCode.PENDING);
        final OrderStatus paymentCompleted = orderStatusRepository.findByStatusName(OrderStatusCode.PAYMENT_COMPLETED);
        final OrderStatus cancel = orderStatusRepository.findByStatusName(OrderStatusCode.CANCELED);

        // 결제성공시 각 상품 주문 상태 수정
        final boolean finalIsAvailableProductService = isAvailableProductService;
        orderDetail.getOrderList().getProductOrderEntityList().stream()
                .filter(productOrder -> productOrder.getStatus().equals(pending.getStatusName()))
                .forEach(productOrder -> productOrder.changeStatus(finalIsAvailableProductService && isPayment ? paymentCompleted.getStatusName() : cancel.getStatusName()));
        orderDetail.changeOrderStatus(finalIsAvailableProductService && isPayment ? paymentCompleted : cancel);
        orderDetail.changeOrderList(orderDetail.getOrderList()); // deepcopy

        orderDetailRepository.save(orderDetail);
        paymentInformationRepository.save(orderCreation.toPaymentInformationEntity(
                orderDetail,
                "1234-5678-1234-5678",
                paymentAmountMap != null ? paymentAmountMap.get(CANCELLED_DISCOUNT_AMOUNT) : 0,
                paymentAmountMap != null ? paymentAmountMap.get(CANCELLED_PAYMENT_AMOUNT) : 0,
                paymentAmountMap != null ? paymentAmountMap.get(CANCELLED_ORIGIN_PRODUCT_PRICE) : 0));

        //통계 테이블 생성
        for(ProductOrderEntity productOrderEntity : orderDetail.getOrderList().getProductOrderEntityList()) {

            //feignClient 필요한 회원정보 요청
            OrderResponse.MemberStatistics memberStatistics = memberServiceFeignClient.getOrderMemberStatistics(orderDetail.getMemberId()).getBody().getResult();

            statisticsRepository.save(Statistics.builder()
                    .customerId(orderDetail.getCustomerId())
                    .memberId(orderDetail.getMemberId())
                    .productId(productOrderEntity.getProductId())
                    .purchaseDate(orderDetail.getOrderDateTime().toLocalDate())
                    .ageRange(memberStatistics.getAgeRange())
                    .gender(memberStatistics.getGender())
                    .quantity(productOrderEntity.getQuantity())
                    .orderType(OrderType.General)
                    .price(productOrderEntity.getFinalPrice())
                    .build());
        }

        return OrderResponse.OfResultPlaceOrder.builder()
                .isPayment(isPayment)
                .paymentAmount(isPayment ? orderCreation.getPaymentAmount() : 0)
                .orderDetailId(orderDetailId)
                .customerId(orderDetail.getCustomerId())
                .build();
    }


    /**
     * 주문상품에 대한 재고 확인 서비스
     * @param orderCreation
     * @param orderDetailId
     * @return
     */
    public ResponseEntity<StockUsageResponse.AvailableResponseList> checkAvailableProductOrder(OrderRequest.OfCreation orderCreation, String orderDetailId) {
        StockUsageRequest.IncreaseStockUsageList increaseStockUsageList = makeRequestObject(orderCreation.getProductOrderList(), orderDetailId);
        return stockFeignClient.checkAvailableOrderProduct(increaseStockUsageList);
    }


    /**
     * 주문상품에 대한 주문상태 변경 서비스
     * @param orderCreation
     * @param results
     * @return
     */
    public Map<String, Integer> updateProductOrder(OrderRequest.OfCreation orderCreation, StockUsageResponse.AvailableResponseList results) {
        Map<Long, OrderRequest.ProductOrder> productOrderMap = orderCreation.getProductOrderList().getProductOrderList()
                .stream().collect(Collectors.toMap(OrderRequest.ProductOrder::getProductId, Function.identity()));

        Map<String, Integer> paymentAmountMap = new HashMap<>();
        paymentAmountMap.put(CANCELLED_PAYMENT_AMOUNT, 0);
        paymentAmountMap.put(CANCELLED_DISCOUNT_AMOUNT, 0);
        paymentAmountMap.put(CANCELLED_ORIGIN_PRODUCT_PRICE, 0);

        for (StockUsageResponse.AvailableStockDto result : results.getAvailableProductInventoryResponseList()) {
            if (!result.getIsAvailableOrder()) {
                OrderRequest.ProductOrder productOrder = productOrderMap.get(result.getProductId());

                productOrder.changeStatus(OrderStatusCode.CANCELED);
                paymentAmountMap.put(CANCELLED_PAYMENT_AMOUNT, paymentAmountMap.get(CANCELLED_PAYMENT_AMOUNT) + productOrder.getFinalPrice());
                paymentAmountMap.put(CANCELLED_DISCOUNT_AMOUNT, paymentAmountMap.get(CANCELLED_DISCOUNT_AMOUNT) + productOrder.getDiscountAmount());
                paymentAmountMap.put(CANCELLED_ORIGIN_PRODUCT_PRICE, paymentAmountMap.get(CANCELLED_ORIGIN_PRODUCT_PRICE) + productOrder.getOriginPrice());
            }
        }
        return paymentAmountMap;
    }

    /**
     * 외부 API 호출을 위한 요청 객체 생성
     *
     * @param productOrderList
     * @param orderDetailId
     * @return
     */
    public StockUsageRequest.IncreaseStockUsageList makeRequestObject(OrderRequest.ProductOrderList productOrderList, String orderDetailId) {
        List<StockUsageRequest.OfIncreasing> stockUsageDtoList = new ArrayList<>();

        for (OrderRequest.ProductOrder product : productOrderList.getProductOrderList()) {
            stockUsageDtoList.add(StockUsageRequest.OfIncreasing.builder()
                    .orderDetailId(orderDetailId)
                    .productId(product.getProductId())
                    .quantity(product.getQuantity())
                    .build());
        }

        StockUsageRequest.IncreaseStockUsageList increaseStockUsageList =
                StockUsageRequest.IncreaseStockUsageList.builder()
                        .ofIncreasingList(stockUsageDtoList)
                        .build();

        return increaseStockUsageList;
    }

    /**
     * 가상 결제 시스템
     *
     * @return
     */
    private boolean checkPaymentValidation() {
        return true;
    }

    /**
     * 현재 날짜 및 시간과 UUID 4자리 조합으로 주문ID 생성
     *
     * @return
     */
    private String makeOrderId() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return timestamp + "-" + uniqueId;
    }

    /**
     * 주문상태 일괄 변경 (상품 준비중 -> 출고 대기)
     * @param bulkUpdateStatus (업데이틀 될 여러 주문 ID 들, 업데이트 될 출고 상태값) DTO
     * @return
     */
    @Transactional
    public List<OrderResponse.OfResultUpdateStatus> changeBulkOrderStatus(Long customerId, OrderRequest.OfBulkUpdateOrderStatus bulkUpdateStatus) {
        // 요청된 모든 주문 상세 정보를 가져옴
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByIdAndCustomerId(bulkUpdateStatus.getOrderIds(), customerId);

        // 요청된 ID 수와 조회된 결과 수가 다르면 존재하지 않는 ID가 있다는 의미
        if (orderDetails.size() != bulkUpdateStatus.getOrderIds().size()) {
            throw new OrderException(ORDER_ID_NOT_FOUND, HttpStatus.CONFLICT);
        }

        OrderStatus requestedStatus = orderStatusRepository.findByStatusName(bulkUpdateStatus.getOrderStatusCode());
        OrderStatusCode requestedStatusCode = requestedStatus.getStatusName();

        for (OrderDetail orderDetail : orderDetails) {

            OrderStatusCode currentOrderStatus = orderDetail.getOrderStatus().getStatusName();

            if (!orderStatusPolicy.getOrderStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(currentOrderStatus)) {
                throw new OrderException(ORDER_STATUS_TRANSITION_RULE_VIOLATION, HttpStatus.CONFLICT);
            }

            // 주문 상태 변경
            switch (bulkUpdateStatus.getOrderStatusCode()) {
                case AWAITING_RELEASE -> {
                    //주문 상태 변경
                    orderDetail.changeOrderStatus(requestedStatus);
                    orderDetail.getOrderList().getProductOrderEntityList().forEach(productOrderEntity ->
                            productOrderEntity.changeStatus(requestedStatusCode));

                    //출고 객체 생성
                    ReleaseStatus releaseStatus = releaseStatusRepository.findByStatusName(ReleaseStatusCode.AWAITING_RELEASE);
                    releaseRepository.save(Release.builder()
                            .orderDetail(orderDetail)
                            .releaseStatus(releaseStatus)
                            .build());
                }
                case PREPARING_PRODUCT, CANCELED, REFUND_REQUEST, REFUNDED -> {
                    orderDetail.changeOrderStatus(requestedStatus);
                    orderDetail.getOrderList().getProductOrderEntityList().forEach(productOrderEntity ->
                            productOrderEntity.changeStatus(requestedStatusCode));
                }
                default -> throw new OrderException(INVALID_ACCESS, HttpStatus.CONFLICT);
            }
            orderDetail.changeOrderList(orderDetail.getOrderList()); // deepcopy
            orderDetailRepository.save(orderDetail); // 명시적 저장

        }
        return orderDetails.stream().map(order -> OrderResponse.OfResultUpdateStatus.builder()
                .orderDetailId(order.getOrderDetailId())
                .orderStatusCode(requestedStatusCode.getCode())
                .productOrderEntityList(order.getOrderList().getProductOrderEntityList().stream().filter(
                        productOrderEntity -> productOrderEntity.getStatus().equals(requestedStatusCode)).collect(Collectors.toList())
                ).build()).collect(Collectors.toList());
    }
}
