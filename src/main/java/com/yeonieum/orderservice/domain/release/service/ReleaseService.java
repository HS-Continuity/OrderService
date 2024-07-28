package com.yeonieum.orderservice.domain.release.service;

import com.yeonieum.orderservice.domain.combinedpackaging.entity.Packaging;
import com.yeonieum.orderservice.domain.combinedpackaging.repository.PackagingRepository;
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
import com.yeonieum.orderservice.domain.release.dto.ReleaseSummaryResponse;
import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.domain.release.entity.ReleaseStatus;
import com.yeonieum.orderservice.domain.release.policy.ReleaseStatusPolicy;
import com.yeonieum.orderservice.domain.release.repository.ReleaseRepository;
import com.yeonieum.orderservice.domain.release.repository.ReleaseStatusRepository;
import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.infrastructure.feignclient.MemberServiceFeignClient;
import com.yeonieum.orderservice.infrastructure.feignclient.ProductServiceFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReleaseService {
    private final ReleaseStatusPolicy releaseStatusPolicy;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseStatusRepository releaseStatusRepository;
    private final MemberServiceFeignClient memberServiceFeignClient;
    private final ProductServiceFeignClient productServiceFeignClient;
    private final OrderStatusPolicy orderStatusPolicy;
    private final DeliveryRepository deliveryRepository;
    private final PackagingRepository packagingRepository;

    /**
     * 상품의 출고 상태 수정 (출고 대기 -> 출고 보류, 출고 대기 -> 출고 완료, 출고 보류 -> 출고 완료)
     * @param updateStatus (주문 ID, 업데이트 될 출고 상태값) DTO
     * @throws IllegalStateException 존재하지 않는 주문 ID인 경우
     * @throws RuntimeException 출고 상태 트랜지션 룰 위반일 경우
     * @throws RuntimeException 주문 상태 트랜지션 룰 위반일 경우
     * @return
     */
    @Transactional
    public void changReleaseStatus (Long customerId, ReleaseRequest.OfUpdateReleaseStatus updateStatus) {
        // 주문 상세 정보 조회
        OrderDetail targetOrderDetail = orderDetailRepository.findById(updateStatus.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID 입니다."));

        if(targetOrderDetail.getCustomerId() != customerId){
            throw new IllegalArgumentException("해당 주문 ID에 대한 권한이 없습니다.");
        }

        //주문내역과 출고는 1:1 관계 -> 주문내역 ID로 찾기
        Release targetRelease = releaseRepository.findByOrderDetailId(updateStatus.getOrderId(), customerId);
        ReleaseStatus requestedStatus = releaseStatusRepository.findByStatusName(updateStatus.getReleaseStatusCode());
        ReleaseStatusCode requestedStatusCode = requestedStatus.getStatusName();

        // 현재 출고 상태
        ReleaseStatusCode releaseStatus = targetRelease.getReleaseStatus().getStatusName();

        //업체의 배송비
        ResponseEntity<ApiResponse<Integer>> response = null;
        Integer deliveryFee = null;
        try {
            response = productServiceFeignClient.retrieveDeliveryFee(targetOrderDetail.getCustomerId());
        } catch (FeignException e) {
            e.printStackTrace();
        }
        if(response != null || response.getStatusCode().is2xxSuccessful()){
            deliveryFee = response.getBody().getResult();
        }

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
                        .deliveryStatus(deliveryStatusRepository.findByStatusName(DeliveryStatusCode.SHIPPED))
                        .shipmentNumber(makeShipNumber())
                        .deliveryFee(deliveryFee)
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
    public Page<ReleaseResponse.OfRetrieve> getReleaseDetailsByCustomerAndStatus(Long customerId, ReleaseStatusCode statusCode, String orderId, LocalDate startDeliveryDate, String recipient, String recipientPhoneNumber, String recipientAddress, String memberId, String memberName, String memberPhoneNumber, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Release> releasesPage = releaseRepository.findReleases(customerId, statusCode, orderId, startDeliveryDate, recipient, recipientPhoneNumber, recipientAddress, memberId, startDate, endDate, pageable);

        List<ReleaseResponse.OfRetrieve> filteredReleases = releasesPage.stream()
                .map(release -> {
                    OrderResponse.MemberInfo memberInfo = null;
                    ResponseEntity<ApiResponse<OrderResponse.MemberInfo>> response = null;
                    try {
                        response = memberServiceFeignClient.getOrderMemberInfo(release.getOrderDetail().getMemberId());
                    } catch (FeignException e) {
                        e.printStackTrace();
                    }
                    if(response != null && response.getStatusCode().is2xxSuccessful()){
                        memberInfo = response.getBody().getResult();
                    }

                    return ReleaseResponse.OfRetrieve.convertedBy(release.getOrderDetail(), release, memberInfo);
                })
                .filter(response -> (memberName == null || response.getMemberInfo().getMemberName().equals(memberName)) &&
                        (memberPhoneNumber == null || response.getMemberInfo().getMemberPhoneNumber().equals(memberPhoneNumber)))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredReleases, pageable, releasesPage.getTotalElements());
    }

    /**
     * 배송시작 날짜 수정
     * @param updateDeliveryDate 배송 시작 날짜 변경 DTO
     */
    @Transactional
    public void changeDeliveryDate (Long customerId, ReleaseRequest.OfUpdateDeliveryDate updateDeliveryDate) {
        Release targetRelease = releaseRepository.findByOrderDetailId(updateDeliveryDate.getOrderId(), customerId);
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
    public void changeReleaseMemo (Long customerId, ReleaseRequest.OfRegisterMemo updateMemo) {
        Release targetRelease = releaseRepository.findByOrderDetailId(updateMemo.getOrderId(), customerId);

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
    public void changeReleaseHoldMemo (ReleaseRequest.OfHoldMemo updateHoldMemo, Long customerId) {

        Release targetRelease = releaseRepository.findByOrderDetailId(updateHoldMemo.getOrderId(), customerId);

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
    public void changeBulkReleaseStatus(ReleaseRequest.OfBulkUpdateReleaseStatus bulkUpdateStatus, Long customerId) {
        // 요청된 모든 주문 상세 정보를 가져옴
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByIdAndCustomerId(bulkUpdateStatus.getOrderIds(), customerId);

        // 요청된 ID 수와 조회된 결과 수가 다르면 존재하지 않는 ID가 있다는 의미
        if (orderDetails.size() != bulkUpdateStatus.getOrderIds().size()) {
            throw new IllegalArgumentException("하나 이상의 주문 ID가 존재하지 않습니다.");
        }


        // 요청된 출고 상태 객체를 가져옴
        ReleaseStatus requestedStatus = releaseStatusRepository.findByStatusName(bulkUpdateStatus.getReleaseStatusCode());
        ReleaseStatusCode requestedStatusCode = requestedStatus.getStatusName();



        // 모든 주문에 대해 상태 변경 수행
        for (OrderDetail orderDetail : orderDetails) {
            Release currentRelease = releaseRepository.findByOrderDetailId(orderDetail.getOrderDetailId(), customerId);
            ReleaseStatusCode currentReleaseStatus =  currentRelease.getReleaseStatus().getStatusName();

            // 출고 상태 전환 규칙 확인
            if (!releaseStatusPolicy.getReleaseStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(currentReleaseStatus)) {
                throw new RuntimeException("출고 상태 전환 규칙 위반!");
            }


            // 출고 상태 업데이트
            currentRelease.changeReleaseStatus(requestedStatus);
            releaseRepository.save(currentRelease);

            // 주문 상태 및 배송 정보 업데이트
            updateOrderAndDeliveryStatus(orderDetail,
                    requestedStatusCode,
                    currentRelease);
        }
    }

    /**
     * 상품의 출고 상태 일괄 수정에 주문 및 배송 상태 변경
     * @param orderDetail 주문내역 엔티티
     * @param requestedStatusCode 요청된 상태 코드
     * @throws RuntimeException 주문 상태 트랜지션 룰 위반일 경우
     * @return
     */
    private void updateOrderAndDeliveryStatus(OrderDetail orderDetail,
                                              ReleaseStatusCode requestedStatusCode,
                                              Release targetRelease) {
        OrderStatus newOrderStatus;

        //업체의 배송비
        ResponseEntity<ApiResponse<Integer>> response = null;
        Integer deliveryFee = null;
        try {
            response = productServiceFeignClient.retrieveDeliveryFee(orderDetail.getCustomerId());
        } catch (FeignException e) {
            e.printStackTrace();
        }
        if(response != null || response.getStatusCode().is2xxSuccessful()){
            deliveryFee = response.getBody().getResult();
        }

        switch (requestedStatusCode) {
            case HOLD_RELEASE:
                //출고 보류 상태로 변경될 시, 주문 상태는 그대로 출고 대기로 유지
                break;
            case RELEASE_COMPLETED:
                if (orderDetail.getOrderStatus().getStatusName() != OrderStatusCode.SHIPPED) {

                    //배송시작일을 입력하지 않을 경우, 출고 완료로 변경 X
                    if(targetRelease.getStartDeliveryDate() == null){
                        throw new IllegalStateException("배송시작일을 입력하지 않으셨습니다!");
                    }

                    // 출고 완료 상태일 경우, 배송 객체 생성
                    Delivery delivery = deliveryRepository.save(Delivery.builder()
                            .deliveryStatus(deliveryStatusRepository.findByStatusName(DeliveryStatusCode.SHIPPED))
                            .shipmentNumber(makeShipNumber())
                            .deliveryFee(deliveryFee)
                            .build());

                    //출고 완료 상태일 경우, 포장 객체 생성
                    packagingRepository.save(Packaging.builder()
                            .release(targetRelease)
                            .orderDetail(orderDetail)
                            .delivery(delivery)
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

    /**
     * 고객의 합포장 신청
     * @param bulkUpdateStatus (업데이틀 될 여러 주문 ID 들, 업데이트 될 출고 상태값) DTO
     * @throws IllegalStateException 존재하지 않는 주문 ID인 경우
     * @throws IllegalStateException 출고 상태가 합포장완료가 아닌 경우
     * @throws RuntimeException 출고 상태 트랜지션 룰 위반일 경우
     * @throws IllegalStateException 출고 상품들의 정보가 모두 일치하지 않은 경우
     * @return
     */
    @Transactional
    public void changeCombinedPackaging(ReleaseRequest.OfBulkUpdateReleaseStatus bulkUpdateStatus, Long customerId) {
        // 요청된 모든 주문 상세 정보를 가져옴
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByIdAndCustomerId(bulkUpdateStatus.getOrderIds(), customerId);

        // 요청된 ID 수와 조회된 결과 수가 다르면 존재하지 않는 ID가 있다는 의미
        if (orderDetails.size() != bulkUpdateStatus.getOrderIds().size()) {
            throw new IllegalArgumentException("하나 이상의 주문 ID가 존재하지 않습니다.");
        }

        // 요청된 출고 상태 객체 정보
        ReleaseStatus requestedStatus = releaseStatusRepository.findByStatusName(bulkUpdateStatus.getReleaseStatusCode());
        ReleaseStatusCode requestedStatusCode = requestedStatus.getStatusName();

        //업체의 배송비
        ResponseEntity<ApiResponse<Integer>> response = null;
        Integer deliveryFee = null;
        try {
            response = productServiceFeignClient.retrieveDeliveryFee(orderDetails.get(0).getCustomerId());
        } catch (FeignException e) {
            e.printStackTrace();
        }
        if(response != null || response.getStatusCode().is2xxSuccessful()){
            deliveryFee = response.getBody().getResult();
        }

        // 상품들의 회원, 배송지, 출고상태, 배송시작일이 같아야 함
        boolean isUniformOrder = orderDetails.stream().allMatch(od ->
                od.getMemberId().equals(orderDetails.get(0).getMemberId()) &&
                        od.getDeliveryAddress().equals(orderDetails.get(0).getDeliveryAddress()) &&
                        releaseRepository.findByOrderDetailId(od.getOrderDetailId(), customerId).getReleaseStatus().equals(releaseRepository.findByOrderDetailId(orderDetails.get(0).getOrderDetailId(), customerId).getReleaseStatus()) &&
                        releaseRepository.findByOrderDetailId(od.getOrderDetailId(), customerId).getStartDeliveryDate().equals(releaseRepository.findByOrderDetailId(orderDetails.get(0).getOrderDetailId(), customerId).getStartDeliveryDate()));


        // 합포장일 경우, 배송 객체를 단 한개만 생성
        Delivery sharedDelivery = null;
        if (requestedStatusCode == ReleaseStatusCode.COMBINED_PACKAGING_COMPLETED) {
            sharedDelivery = deliveryRepository.save(Delivery.builder()
                    .deliveryStatus(deliveryStatusRepository.findByStatusName(DeliveryStatusCode.SHIPPED))
                    .shipmentNumber(makeShipNumber())
                    .deliveryFee(deliveryFee)
                    .build());
        } else {
            throw new IllegalStateException("업데이트 할 출고 상태 코드가 합포장인지 확인해주세요.");
        }

        if(isUniformOrder){
            // 모든 주문에 대해 상태 변경 수행
            for (OrderDetail orderDetail : orderDetails) {
                Release currentRelease = releaseRepository.findByOrderDetailId(orderDetail.getOrderDetailId(), customerId);
                ReleaseStatusCode currentReleaseStatus =  currentRelease.getReleaseStatus().getStatusName();

                // 출고 상태 전환 규칙 확인
                if (!releaseStatusPolicy.getReleaseStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(currentReleaseStatus)) {
                    throw new RuntimeException("출고 상태 전환 규칙 위반!");
                }

                // 출고 상태 업데이트
                currentRelease.changeReleaseStatus(requestedStatus);
                releaseRepository.save(currentRelease);

                //상품 각각의 포장객체 생성
                packagingRepository.save(Packaging.builder()
                        .release(currentRelease)
                        .orderDetail(orderDetail)
                        .delivery(sharedDelivery)
                        .build());

                // 주문 상태를 배송 시작으로 변경
                OrderStatus newOrderStatus = orderStatusRepository.findByStatusName(OrderStatusCode.SHIPPED);
                orderDetail.changeOrderStatus(newOrderStatus);

                // 연관된 모든 상품 주문의 상태를 배송 시작으로 변경
                orderDetail.getOrderList().getProductOrderEntityList().forEach(productOrder -> {
                    productOrder.changeStatus(newOrderStatus.getStatusName());
                });
                orderDetailRepository.save(orderDetail);
            }
        } else {
            throw new IllegalStateException("선택한 상품들의 회원, 배송지, 배송일, 출고 상태가 같은지 확인해주세요.");
        }
    }

    /**
     * 상품들의 출고 상태별 카운팅
     * @param customerId 고객 ID
     * @return 상품의 출고 상태별 카운팅 수
     */
    public List<ReleaseSummaryResponse> countReleaseStatus(Long customerId) {
        return releaseRepository.countByReleaseStatus(customerId);
    }

    /**
     * 운송장 번호 랜덤 생성 메서드
     * @return 운송장 번호
     */
    private String makeShipNumber() {
        // 현재 시간 밀리초 단위로 가져오기
        long currentTimeMillis = System.currentTimeMillis();

        // 밀리초 단위를 날짜 형식으로 변환
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String datePart = sdf.format(new Date(currentTimeMillis));

        // 랜덤한 4자리 문자열 생성
        String randomPart = generateRandomString(4);

        // 날짜 형식과 랜덤 문자열 결합하여 운송장 번호 생성
        return datePart + randomPart;
    }

    /**
     * 랜덤한 문자열을 생성하는 메서드
     * @param length 랜덤 문자열 길이
     * @return 랜덤한 문자열
     */
    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }

        return sb.toString();
    }
}
