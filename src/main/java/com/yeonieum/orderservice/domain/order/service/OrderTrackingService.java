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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import java.util.*;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {
    private final OrderDetailRepository orderDetailRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final MemberServiceFeignClient memberServiceFeignClient;
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

        List<String> filteredMemberIds = null;

        if (memberName != null || memberPhoneNumber != null) {
            // 멤버 이름과 전화번호로 필터링하여 필요한 멤버 ID들을 먼저 수집
            filteredMemberIds = memberServiceFeignClient.getOrderMemberFilter(memberName, memberPhoneNumber).getBody().getResult();
            if (filteredMemberIds.isEmpty()) {
                // 필터링된 멤버 ID가 없으면 비어 있는 페이지 반환
                return Page.empty(pageable);
            }
        }

        Page<OrderDetail> orderDetailsPage = orderDetailRepository.findOrders(customerId, orderStatusCode, orderDetailId, orderDateTime, recipient, recipientPhoneNumber, recipientAddress, memberId, filteredMemberIds, startDate, endDate, pageable);

        List<OrderResponse.OfRetrieveForCustomer> convertedOrders = new ArrayList<>();
        List<Long> productIdList = orderDetailsPage.stream()
                .flatMap(orderDetail -> orderDetail.getOrderList()
                        .getProductOrderEntityList()
                        .stream()
                        .map(ProductOrderEntity::getProductId))
                .collect(Collectors.toList());

        boolean isAvailableProductService = true;
        ResponseEntity<ApiResponse<Set<RetrieveOrderInformationResponse>>> productResponse = null;

        try{
            productResponse = productServiceFeignClient.retrieveOrderProductInformation(productIdList);
            isAvailableProductService = productResponse.getStatusCode().is2xxSuccessful();
        } catch (FeignException e) {
            e.printStackTrace();
            isAvailableProductService = false;
        }

        ResponseEntity<ApiResponse<OrderResponse.MemberInfo>> memberResponse = null;
        for (OrderDetail orderDetail : orderDetailsPage) {
            boolean isAvailableMemberService = true;
            try{
                memberResponse = memberServiceFeignClient.getOrderMemberInfo(orderDetail.getMemberId());
            } catch (FeignException e) {
                isAvailableMemberService = false;
            }
            OrderResponse.MemberInfo memberInfo = memberResponse == null ? null : memberResponse.getBody().getResult();

            OrderResponse.OfRetrieveForCustomer orderResponse =
                    OrderResponse.OfRetrieveForCustomer.convertedBy(orderDetail, memberInfo, isAvailableProductService, isAvailableMemberService);

            if(isAvailableProductService) {
                Set<RetrieveOrderInformationResponse> productInformation = productResponse.getBody().getResult();
                final Map<Long, RetrieveOrderInformationResponse> productInformationMap = new HashMap<>();

                for(RetrieveOrderInformationResponse product : productInformation) {
                    productInformationMap.put(product.getProductId(), product);
                }

                orderResponse.getProductOrderList().getProductOrderList().stream().map(
                        productOrder -> {
                            productOrder.changeName(productInformationMap.get(productOrder.getProductId()).getProductName());
                            return productOrder;
                        }).collect(Collectors.toList());
            }
            convertedOrders.add(orderResponse);
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
        ResponseEntity<ApiResponse<Set<RetrieveOrderInformationResponse>>> productResponse = null;
        try {
            productResponse = productServiceFeignClient.retrieveOrderProductInformation(productIdList);
            isAvailableProductService = productResponse.getStatusCode().is2xxSuccessful();
        } catch (FeignException e) {
            e.printStackTrace();
            isAvailableProductService = false;
        }

        if(isAvailableProductService) {
            Set<RetrieveOrderInformationResponse> productInformationList = productResponse.getBody().getResult();
            Map<Long, RetrieveOrderInformationResponse> productInformationMap = new HashMap();
            for(RetrieveOrderInformationResponse productInformation : productInformationList) {
                System.out.println(productInformation.getProductImage());
            }
            productInformationList.forEach(productInformation -> {
                productInformationMap.put(productInformation.getProductId(), productInformation);
            });

            return orderDetailsPage.map(orderDetail -> {
                System.out.println(orderDetail.getMainProductId());
                return OrderResponse.OfRetrieveForMember
                        .convertedBy(orderDetail, productInformationMap.get(orderDetail.getMainProductId()), true);
            });
        }

        return orderDetailsPage.map(orderDetail -> OrderResponse.OfRetrieveForMember
                    .convertedBy(orderDetail, null, false));
    }
}