package com.yeonieum.orderservice.domain.release.service;

import com.yeonieum.orderservice.domain.delivery.entity.Delivery;
import com.yeonieum.orderservice.domain.delivery.repository.DeliveryRepository;
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

import java.util.List;

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
    private final DeliveryRepository deliveryRepository;

    /**
     * 상품의 출고 상태 수정 (출고 대기 -> 출고 보류, 출고 대기 -> 출고 완료, 출고 보류 -> 출고 완료)
     * @param updateStatus (주문 ID, 업데이트 될 출고 상태값) DTO
     * @throws IllegalStateException 존재하지 않는 주문 ID인 경우
     * @throws RuntimeException 출고 상태 트랜지션 룰 위반일 경우
     * @throws RuntimeException 주문 상태 트랜지션 룰 위반일 경우
     * @return
     */
    @Transactional
    public void changReleaseStatus (ReleaseRequest.OfUpdateReleaseStatus updateStatus) {
        // 주문 상세 정보 조회
        OrderDetail targetOrderDetail = orderDetailRepository.findById(updateStatus.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID 입니다."));

        //주문내역과 출고는 1:1 관계 -> 주문내역 ID로 찾기
        Release targetRelease = releaseRepository.findByOrderDetailId(updateStatus.getOrderId());
        ReleaseStatus requestedStatus = releaseStatusRepository.findByStatusName(updateStatus.getReleaseStatusCode());
        ReleaseStatusCode requestedStatusCode = requestedStatus.getStatusName();

        // 현재 출고 상태
        ReleaseStatusCode releaseStatus = targetRelease.getReleaseStatus().getStatusName();

        // 출고 상태 전환 규칙 검증
        if(!releaseStatusPolicy.getReleaseStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(releaseStatus)) {
            throw new RuntimeException("출고상태 트랜지션 룰 위반!");
        }

        OrderStatus presentOrderStatus = null;
        OrderStatusCode presentOrderStatusCode = null;
        OrderStatusCode orderStatus = targetOrderDetail.getOrderStatus().getStatusName();

        // 출고 상태 변경 로직
        switch (updateStatus.getReleaseStatusCode()) {
            //출고 보류
            case HOLD_RELEASE -> {
                //출고객체의 출고 상태 '출고 보류'로 변경
                targetRelease.changeReleaseStatus(requestedStatus);

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

                // 주문 상태 업데이트
                targetOrderDetail.changeOrderStatus(presentOrderStatus);

                for (ProductOrderEntity productOrderEntity : targetOrderDetail.getOrderList().getProductOrderEntityList()) {
                    productOrderEntity.changeStatus(presentOrderStatusCode);
                }
            }
            default -> throw new RuntimeException("잘못된 접근입니다.");
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
    @Transactional
    public Page<ReleaseResponse.OfRetrieve> getReleaseDetailsByCustomerAndStatus(Long customerId, ReleaseStatusCode statusCode, Pageable pageable) {
        Page<Release> releasesPage;
        if (statusCode == null) {
            releasesPage = releaseRepository.findByCustomerId(customerId, pageable);
        } else {
            releasesPage = releaseRepository.findByCustomerIdAndStatus(customerId, statusCode, pageable);
        }

        return releasesPage.map(release -> {
            OrderResponse.MemberInfo memberInfo = feignClient.getOrderMemberInfo(release.getOrderDetail().getMemberId()).getBody().getResult();
            return ReleaseResponse.OfRetrieve.convertedBy(release.getOrderDetail(),release, memberInfo);
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
            throw new IllegalArgumentException("존재하지 않는 주문 ID 입니다.");
        }

        targetRelease.changeStartDeliveryDate(updateDeliveryDate.getStartDeliveryDate());
        releaseRepository.save(targetRelease);
    }

    /**
     * 출고 메모 작성
     * @param updateMemo 출고 메모 작성 DTO
     * @throws IllegalStateException 존재하지 않는 주문 ID인 경우
     */
    @Transactional
    public void changeReleaseMemo (ReleaseRequest.OfRegisterMemo updateMemo) {

        Release targetRelease = releaseRepository.findByOrderDetailId(updateMemo.getOrderId());

        if (targetRelease == null) {
            throw new IllegalArgumentException("존재하지 않는 주문 ID 입니다.");
        }

        targetRelease.changeReleaseMemo(updateMemo.getMemo());
        releaseRepository.save(targetRelease);
    }

    /**
     * 출고 보류 사유 메모 작성 (출고 상태가 '출고 보류'일 경우만 작성 가능)
     * @throws IllegalStateException 존재하지 않는 주문 ID인 경우
     * @throws IllegalStateException 출고 상태가 '출고 보류'가 아닌 경우
     * @param updateHoldMemo 출고 보류 메모 작성 DTO
     */
    @Transactional
    public void changeReleaseHoldMemo (ReleaseRequest.OfHoldMemo updateHoldMemo) {

        Release targetRelease = releaseRepository.findByOrderDetailId(updateHoldMemo.getOrderId());

        if (targetRelease == null) {
            throw new IllegalArgumentException("존재하지 않는 주문 ID 입니다.");
        }

        if(targetRelease.getReleaseStatus().getStatusName() != ReleaseStatusCode.HOLD_RELEASE){
            throw new IllegalStateException("현재 출고 상태가 '출고 보류' 상태가 아닙니다.");
        }

        targetRelease.changeReleaseHoldReason(updateHoldMemo.getMemo());
        releaseRepository.save(targetRelease);
    }

    /**
     * 상품의 출고 상태 일괄 수정 (출고 대기 -> 출고 보류, 출고 대기 -> 출고 완료, 출고 보류 -> 출고 완료)
     * @param bulkUpdateStatus (업데이틀 될 여러 주문 ID 들, 업데이트 될 출고 상태값) DTO
     * @throws IllegalStateException 존재하지 않는 주문 ID인 경우
     * @throws RuntimeException 출고 상태 트랜지션 룰 위반일 경우
     * @throws RuntimeException 주문 상태 트랜지션 룰 위반일 경우
     * @return
     */
    @Transactional
    public void changeBulkReleaseStatus(ReleaseRequest.OfBulkUpdateReleaseStatus bulkUpdateStatus) {
        // 요청된 모든 주문 상세 정보를 가져옴
        List<OrderDetail> orderDetails = orderDetailRepository.findAllById(bulkUpdateStatus.getOrderIds());

        // 요청된 ID 수와 조회된 결과 수가 다르면 존재하지 않는 ID가 있다는 의미
        if (orderDetails.size() != bulkUpdateStatus.getOrderIds().size()) {
            throw new IllegalArgumentException("하나 이상의 주문 ID가 존재하지 않습니다.");
        }

        // 요청된 출고 상태 객체를 가져옴
        ReleaseStatus requestedStatus = releaseStatusRepository.findByStatusName(bulkUpdateStatus.getReleaseStatusCode());
        ReleaseStatusCode requestedStatusCode = requestedStatus.getStatusName();

        // 모든 주문에 대해 상태 변경 수행
        for (OrderDetail orderDetail : orderDetails) {
            Release targetRelease = releaseRepository.findByOrderDetailId(orderDetail.getOrderDetailId());
            ReleaseStatusCode currentReleaseStatus =  targetRelease.getReleaseStatus().getStatusName();

            // 출고 상태 전환 규칙 확인
            if (!releaseStatusPolicy.getReleaseStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(currentReleaseStatus)) {
                throw new RuntimeException("출고 상태 전환 규칙 위반!");
            }

            // 출고 상태 업데이트
            targetRelease.changeReleaseStatus(requestedStatus);
            releaseRepository.save(targetRelease);

            // 주문 상태 및 배송 정보 업데이트
            updateOrderAndDeliveryStatus(orderDetail, requestedStatusCode);
        }
    }

    /**
     * 상품의 출고 상태 일괄 수정에 주문 및 배송 상태 변경
     * @param orderDetail 주문내역 엔티티
     * @param requestedStatusCode 요청된 상태 코드
     * @throws RuntimeException 주문 상태 트랜지션 룰 위반일 경우
     * @return
     */
    private void updateOrderAndDeliveryStatus(OrderDetail orderDetail, ReleaseStatusCode requestedStatusCode) {
        OrderStatus newOrderStatus;

        switch (requestedStatusCode) {
            case HOLD_RELEASE:
                //출고 보류 상태로 변경될 시, 주문 상태는 그래도 출고 대기로 유지
                break;
            case RELEASE_COMPLETED:
                if (orderDetail.getOrderStatus().getStatusName() != OrderStatusCode.SHIPPED) {
                    // 출고 완료 상태일 경우, 배송 시작 처리
                    deliveryRepository.save(Delivery.builder()
                            .orderDetail(orderDetail)
                            .deliveryStatus(deliveryStatusRepository.findByStatusName(DeliveryStatusCode.SHIPPED))
                            .build());


                    // 주문 상태를 배송 시작으로 변경
                    newOrderStatus = orderStatusRepository.findByStatusName(OrderStatusCode.SHIPPED);
                    orderDetail.changeOrderStatus(newOrderStatus);

                    // 연관된 모든 상품 주문의 상태를 배송 시작으로 변경
                    orderDetail.getOrderList().getProductOrderEntityList().forEach(productOrder -> {
                        productOrder.changeStatus(newOrderStatus.getStatusName());
                    });
                }
                break;
            default:
                throw new IllegalArgumentException("잘못된 출고 상태 코드입니다.");
        }
        orderDetailRepository.save(orderDetail);
    }
}
