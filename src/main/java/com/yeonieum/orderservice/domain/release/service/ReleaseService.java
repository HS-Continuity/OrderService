package com.yeonieum.orderservice.domain.release.service;

import com.yeonieum.orderservice.domain.delivery.entity.Delivery;
import com.yeonieum.orderservice.domain.delivery.repository.DeliveryStatusRepository;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.OrderStatus;
import com.yeonieum.orderservice.domain.order.repository.OrderDetailRepository;
import com.yeonieum.orderservice.domain.order.repository.OrderStatusRepository;
import com.yeonieum.orderservice.domain.release.dto.ReleaseRequest;
import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.domain.release.entity.ReleaseStatus;
import com.yeonieum.orderservice.domain.release.repository.ReleaseRepository;
import com.yeonieum.orderservice.domain.release.repository.ReleaseStatusRepository;
import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.yeonieum.orderservice.domain.order.policy.OrderStatusPolicy.orderStatusTransitionRule;
import static com.yeonieum.orderservice.domain.release.policy.ReleaseStatusPolicy.releaseStatusTransitionRule;

@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseStatusRepository releaseStatusRepository;

    /**
     * 상품의 출고 상태 수정
     * @param updateStatus (주문 ID, 업데이트 될 출고 상태값) DTO
     * @return
     */
    @Transactional
    public void changReleaseStatus (ReleaseRequest.OfUpdateReleaseStatus updateStatus) {

        OrderDetail targetOrderDetail = orderDetailRepository.findById(updateStatus.getOrderId())
                .orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 주문 ID 입니다."));

        //주문내역과 출고는 1:1 관계 -> 주문내역 ID로 찾기
        Release targetRelease = releaseRepository.findByOrderDetailId(updateStatus.getOrderId());

        ReleaseStatus releaseStatus = releaseStatusRepository.findByStatusName(updateStatus.getReleaseStatusCode());

        OrderStatus requestedStatus = null;

        OrderStatusCode requestedStatusCode = requestedStatus.getStatusName();
        OrderStatusCode orderStatus = targetOrderDetail.getOrderStatus().getStatusName();

        if(!orderStatusTransitionRule.get(requestedStatusCode).getRequiredPreviosConditionSet().contains(orderStatus)) {
            throw new RuntimeException("주문상태 트랜지션 룰 위반!");
        }

        if(!releaseStatusTransitionRule.get(updateStatus.getReleaseStatusCode()).getRequiredPreviosConditionSet().contains(releaseStatus)) {
            throw new RuntimeException("출고상태 트랜지션 룰 위반!");
        }

        switch (updateStatus.getReleaseStatusCode()) {
            //출고 보류
            case HOLD_RELEASE -> {
                //출고객체의 출고 상태 '출고 보류'로 변경
                targetRelease.changeReleaseStatus(releaseStatus);

                //출고객체의 출고 상태가 '출고 보류'가 되더라도 주문 상태는 '출고 대기'로 유지
                requestedStatus = orderStatusRepository.findByStatusName(OrderStatusCode.AWAITING_RELEASE);
            }
            //출고 완료 -> 배송 객체 생성 (배송시작)
            case RELEASE_COMPLETED -> {
                //출고객체의 출고 상태 '출고 완료'로 변경
                targetRelease.changeReleaseStatus(releaseStatus);

                //배송객체 '배송시작'상태로 생성
                Delivery.builder()
                        .orderDetail(targetOrderDetail)
                        .deliveryStatus(deliveryStatusRepository.findByStatusName(DeliveryStatusCode.SHIPPED))
                        .build();

                //출고가 완료되면, 배송 시작 -> 주문 상태는 '배송 시작'으로 변경
                requestedStatus = orderStatusRepository.findByStatusName(OrderStatusCode.SHIPPED);
            }
            default -> throw new RuntimeException("잘못된 접근입니다.");
        }

        // 주문 상태 변경
        targetOrderDetail.changeOrderStatus(requestedStatus);
        targetOrderDetail.getOrderList().getProductOrderEntityList().forEach(productOrderEntity ->
                productOrderEntity.changeStatus(requestedStatusCode));

        // 명시적 저장
        orderDetailRepository.save(targetOrderDetail);
        releaseRepository.save(targetRelease);
    }
}
