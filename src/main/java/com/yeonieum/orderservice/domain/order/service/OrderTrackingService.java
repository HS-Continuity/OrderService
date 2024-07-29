package com.yeonieum.orderservice.domain.order.service;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.ProductOrderEntity;
import com.yeonieum.orderservice.domain.order.repository.OrderDetailRepository;
import com.yeonieum.orderservice.domain.order.repository.OrderStatusRepository;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.infrastructure.feignclient.MemberServiceFeignClient;
import com.yeonieum.orderservice.infrastructure.feignclient.ProductServiceFeignClient;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.RetrieveOrderInformationResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {
    private final OrderDetailRepository orderDetailRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final MemberServiceFeignClient memberFeignClient;
    private final ProductServiceFeignClient productServiceFeignClient;

    /**
     * 고객용 주문 조회 서비스
     * @param customerId
     * @param orderStatusCode
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse.OfRetrieveForCustomer> retrieveOrdersForCustomer(Long customerId, OrderStatusCode orderStatusCode, String orderDetailId, LocalDateTime orderDateTime, String recipient, String recipientPhoneNumber, String recipientAddress, String memberId, String memberName, String memberPhoneNumber, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<OrderDetail> orderDetailsPage =
                orderDetailRepository.findOrders(customerId, orderStatusCode, orderDetailId, orderDateTime, recipient, recipientPhoneNumber, recipientAddress, memberId, startDate, endDate, pageable);

        List<OrderResponse.OfRetrieveForCustomer> convertedOrders = new ArrayList<>();
        List<Long> productIdList = orderDetailsPage.stream()
                .flatMap(orderDetail -> orderDetail.getOrderList()
                        .getProductOrderEntityList()
                        .stream()
                        .map(ProductOrderEntity::getProductId))
                .collect(Collectors.toList());

        boolean isAvailableProductService = true;
        ResponseEntity<ApiResponse<List<RetrieveOrderInformationResponse>>> productResponse = null;

        try{
            productResponse = productServiceFeignClient.retrieveOrderProductInformation(productIdList);
            isAvailableProductService = productResponse.getStatusCode().is2xxSuccessful();
        } catch (FeignException e) {
            isAvailableProductService = false;
        }

        ResponseEntity<ApiResponse<OrderResponse.MemberInfo>> memberResponse = null;
        for (OrderDetail orderDetail : orderDetailsPage) {
            boolean isAvailableMemberService = true;
            try{
                memberResponse = memberFeignClient.getOrderMemberInfo(orderDetail.getMemberId());
            } catch (FeignException e) {
                isAvailableMemberService = false;
            }
            OrderResponse.MemberInfo memberInfo = memberResponse == null ? null : memberResponse.getBody().getResult();

            // 필터링 조건을 확인하여 필요한 경우 필터링
            if (memberInfo != null &&
                    (memberName == null || memberName.equals(memberInfo.getMemberName())) &&
                    (memberPhoneNumber == null || memberPhoneNumber.equals(memberInfo.getMemberPhoneNumber()))) {

                OrderResponse.OfRetrieveForCustomer orderResponse =
                        OrderResponse.OfRetrieveForCustomer.convertedBy(orderDetail, memberInfo, isAvailableProductService, isAvailableMemberService);

                if(isAvailableProductService) {
                    List<RetrieveOrderInformationResponse> productInformation = productResponse.getBody().getResult();
                    final Map<Long, RetrieveOrderInformationResponse> productInformationMap =
                            productInformation.stream().collect(Collectors.toMap(RetrieveOrderInformationResponse::getProductId, product -> product));

                    orderResponse.getProductOrderList().getProductOrderList().stream().map(
                            productOrder -> {
                                productOrder.changeName(productInformationMap.get(productOrder.getProductId()).getProductName());
                                return productOrder;
                            }).collect(Collectors.toList());
                }
                convertedOrders.add(orderResponse);
            }
        }

        return new PageImpl<>(convertedOrders, pageable, orderDetailsPage.getTotalElements());
    }

    /**
     * 고객용 주문상태별 주문 건수 조회 서비스
     * @param customerId
     * @param orderStatusCode
     * @return
     */
    @Transactional(readOnly = true)
    public Long retrieveTotalOrderCountForCustomer(Long customerId, OrderStatusCode orderStatusCode) {
        return orderDetailRepository.countByCustomerIdAndOrderStatus(customerId, orderStatusRepository.findByStatusName(orderStatusCode));
    }

    /**
     * 회원용 주문 조회 서비스
     * @param memberId
     * @param startDate
     * @param endDate
     * @param pageable
     * @return
     */
    // 대표 상품에 대해서만 가져오기
    @Transactional(readOnly = true)
    public Page<OrderResponse.OfRetrieveForMember> retrieveOrderForMember(String memberId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<OrderDetail> orderDetailsPage =
                orderDetailRepository.findByMemberId(memberId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59), pageable);
        List<Long> productIdList = orderDetailsPage.getContent().stream().map(orderDetail -> orderDetail.getMainProductId()).collect(Collectors.toList());

        boolean isAvailableProductService = true;
        ResponseEntity<ApiResponse<List<RetrieveOrderInformationResponse>>> productResponse = null;
        try {
            productResponse = productServiceFeignClient.retrieveOrderProductInformation(productIdList);
            isAvailableProductService = productResponse.getStatusCode().is2xxSuccessful();
        } catch (FeignException e) {
            e.printStackTrace();
            isAvailableProductService = false;
        }

        if(isAvailableProductService) {
            List<RetrieveOrderInformationResponse> productInformationList = productResponse.getBody().getResult();
            Map<Long, RetrieveOrderInformationResponse> productInformationMap =
                    productInformationList.stream().collect(Collectors.toMap(RetrieveOrderInformationResponse::getProductId, product -> product));

            return orderDetailsPage.map(orderDetail -> OrderResponse.OfRetrieveForMember
                    .convertedBy(orderDetail, productInformationMap.get(orderDetail.getMainProductId()), true));
        }


        return orderDetailsPage.map(orderDetail -> OrderResponse.OfRetrieveForMember
                    .convertedBy(orderDetail, null, false));
    }
}