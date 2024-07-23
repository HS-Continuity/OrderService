package com.yeonieum.orderservice.domain.release.service;

import com.yeonieum.orderservice.domain.delivery.entity.Delivery;
import com.yeonieum.orderservice.domain.delivery.repository.DeliveryStatusRepository;
import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.OrderStatus;
import com.yeonieum.orderservice.domain.order.entity.ProductOrderEntity;
import com.yeonieum.orderservice.domain.order.policy.OrderStatusPolicy;
import com.yeonieum.orderservice.domain.order.repository.OrderDetailRepository;
import com.yeonieum.orderservice.domain.order.repository.OrderStatusRepository;
import com.yeonieum.orderservice.domain.release.dto.ReleaseRequest;
import com.yeonieum.orderservice.domain.release.dto.ReleaseResponse;
import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.domain.release.entity.ReleaseStatus;
import com.yeonieum.orderservice.domain.release.policy.ReleaseStatusPolicy;
import com.yeonieum.orderservice.domain.release.repository.ReleaseRepository;
import com.yeonieum.orderservice.domain.release.repository.ReleaseStatusRepository;
import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import com.yeonieum.orderservice.infrastructure.feignclient.MemberServiceFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReleaseService {
    private final ReleaseStatusPolicy releaseStatusPolicy;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseStatusRepository releaseStatusRepository;
    private final MemberServiceFeignClient feignClient;
    private final OrderStatusPolicy orderStatusPolicy;

    /**
     * 상품의 출고 상태 수정 (출고 대기 -> 출고 보류, 출고 대기 -> 출고 완료, 출고 보류 -> 출고 완료)
     * @param updateStatus (주문 ID, 업데이트 될 출고 상태값) DTO
     * @return
     */
    @Transactional
    public void changReleaseStatus (ReleaseRequest.OfUpdateReleaseStatus updateStatus) {

        OrderDetail targetOrderDetail = orderDetailRepository.findById(updateStatus.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID 입니다."));

        //주문내역과 출고는 1:1 관계 -> 주문내역 ID로 찾기
        Release targetRelease = releaseRepository.findByOrderDetailId(updateStatus.getOrderId());
        ReleaseStatus requestedStatus = releaseStatusRepository.findByStatusName(updateStatus.getReleaseStatusCode());
        ReleaseStatusCode requestedStatusCode = requestedStatus.getStatusName();

        ReleaseStatusCode releaseStatus = targetRelease.getReleaseStatus().getStatusName();

        if(!releaseStatusPolicy.getReleaseStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(releaseStatus)) {
            throw new RuntimeException("출고상태 트랜지션 룰 위반!");
        }

        OrderStatus presentOrderStatus = null;
        OrderStatusCode presentOrderStatusCode = null;
        OrderStatusCode orderStatus = targetOrderDetail.getOrderStatus().getStatusName();
        switch (updateStatus.getReleaseStatusCode()) {
            //출고 보류
            case HOLD_RELEASE -> {
                //출고객체의 출고 상태 '출고 보류'로 변경
                targetRelease.changeReleaseStatus(requestedStatus);

                //출고객체의 출고 상태가 '출고 보류'가 되더라도 주문 상태는 '출고 대기'로 유지
                presentOrderStatus = orderStatusRepository.findByStatusName(OrderStatusCode.AWAITING_RELEASE);
                presentOrderStatusCode = OrderStatusCode.AWAITING_RELEASE;
                if(!orderStatusPolicy.getOrderStatusTransitionRule().get(presentOrderStatus.getStatusName()).getRequiredPreviosConditionSet().contains(orderStatus)) {
                    throw new RuntimeException("주문상태 트랜지션 룰 위반!");
                }
            }
            //출고 완료 -> 배송 객체 생성 (배송시작)
            case RELEASE_COMPLETED -> {

                //배송시작일을 입력하지 않을 경우, 출고 완료로 변경 X
                if(targetRelease.getStartDeliveryDate() == null){
                    throw new IllegalStateException("배송시작일을 입력하지 않으셨습니다!");
                }

                //출고객체의 출고 상태 '출고 완료'로 변경
                targetRelease.changeReleaseStatus(requestedStatus);

                //배송객체 '배송시작'상태로 생성
                Delivery.builder()
                        .orderDetail(targetOrderDetail)
                        .deliveryStatus(deliveryStatusRepository.findByStatusName(DeliveryStatusCode.SHIPPED))
                        .build();

                //출고가 완료되면, 배송 시작 -> 주문 상태는 '배송 시작'으로 변경
                presentOrderStatus = orderStatusRepository.findByStatusName(OrderStatusCode.SHIPPED);
                presentOrderStatusCode = OrderStatusCode.SHIPPED;
                if(!orderStatusPolicy.getOrderStatusTransitionRule().get(presentOrderStatus.getStatusName()).getRequiredPreviosConditionSet().contains(orderStatus)) {
                    throw new RuntimeException("주문상태 트랜지션 룰 위반!");
                }
            }
            default -> throw new RuntimeException("잘못된 접근입니다.");
        }

        // 주문 상태 변경
        targetOrderDetail.changeOrderStatus(presentOrderStatus);

        for (ProductOrderEntity productOrderEntity : targetOrderDetail.getOrderList().getProductOrderEntityList()) {
            productOrderEntity.changeStatus(presentOrderStatusCode);
        }

        // 명시적 저장
        orderDetailRepository.save(targetOrderDetail);
        releaseRepository.save(targetRelease);
    }

    /**
     * 고객 출고상품 조회 서비스
     * @param customerId 고객 ID
     * @param statusCode 출고 상태 코드
     * @param pageable 페이지 요청 정보
     * @return 페이지 처리된 출고 상품 응답 객체
     */
    @Transactional(readOnly = true)
    public Page<ReleaseResponse.OfRetrieve> getReleaseDetailsByCustomerAndStatus(Long customerId, ReleaseStatusCode statusCode, Pageable pageable) {
        Page<Release> releasesPage;
        if (statusCode == null) {
            releasesPage = releaseRepository.findByCustomerId(customerId, pageable);
        } else {
            releasesPage = releaseRepository.findByCustomerIdAndStatus(customerId, statusCode, pageable);
        }

        return releasesPage.map(release -> {
            OrderResponse.MemberInfo targetMember = feignClient.getOrderMemberInfo(release.getOrderDetail().getMemberId()).getBody().getResult();
            return ReleaseResponse.OfRetrieve.builder()
                    .memberInfo(targetMember)
                    .startDeliveryDate(release.getStartDeliveryDate())
                    .productOrderList(OrderResponse.ProductOrderList.convertedBy(release.getOrderDetail()))
                    .build();
        });
    }

    /**
     * 배송시작 날짜 수정
     * @param updateDeliveryDate 배송 시작 날짜 변경 DTO
     */
    @Transactional
    public void changeDeliveryDate (ReleaseRequest.OfUpdateDeliveryDate updateDeliveryDate) {

        Release targetRelease = releaseRepository.findByOrderDetailId(updateDeliveryDate.getOrderId());

        if (targetRelease == null) {
            throw new IllegalArgumentException("존재하지 않는 주문 ID: " + updateDeliveryDate.getOrderId());
        }

        targetRelease.changeStartDeliveryDate(updateDeliveryDate.getStartDeliveryDate());
        releaseRepository.save(targetRelease);
    }
}
