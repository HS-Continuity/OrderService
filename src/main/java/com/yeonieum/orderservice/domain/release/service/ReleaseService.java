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
import com.yeonieum.orderservice.domain.order.exception.OrderException;
import com.yeonieum.orderservice.domain.order.policy.OrderStatusPolicy;
import com.yeonieum.orderservice.domain.order.repository.OrderDetailRepository;
import com.yeonieum.orderservice.domain.order.repository.OrderStatusRepository;
import com.yeonieum.orderservice.domain.release.dto.ReleaseRequest;
import com.yeonieum.orderservice.domain.release.dto.ReleaseResponse;
import com.yeonieum.orderservice.domain.release.dto.ReleaseSummaryResponse;
import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.domain.release.entity.ReleaseStatus;
import com.yeonieum.orderservice.domain.release.exception.ReleaseException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static com.yeonieum.orderservice.domain.order.exception.OrderExceptionCode.*;
import static com.yeonieum.orderservice.domain.release.exception.ReleaseExceptionCode.*;
import static com.yeonieum.orderservice.domain.release.exception.ReleaseExceptionCode.INVALID_ACCESS;

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
     * @throws OrderException 존재하지 않는 주문 ID인 경우
     * @throws ReleaseException 출고 상태 트랜지션 룰 위반일 경우
     * @throws OrderException 주문 상태 트랜지션 룰 위반일 경우
     * @return
     */
    @Transactional
    public void changReleaseStatus (Long customerId, ReleaseRequest.OfUpdateReleaseStatus updateStatus) {
        // 주문 상세 정보 조회
        OrderDetail targetOrderDetail = orderDetailRepository.findById(updateStatus.getOrderId())
                .orElseThrow(() -> new OrderException(ORDER_NOT_FOUND, HttpStatus.NOT_FOUND));

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
            throw new ReleaseException(RELEASE_STATUS_TRANSITION_RULE_VIOLATION, HttpStatus.CONFLICT);
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
                    throw new ReleaseException(START_DELIVERY_DATE_NOT_PROVIDED, HttpStatus.CONFLICT);
                }

                //출고객체의 출고 상태 '출고 완료'로 변경
                targetRelease.changeReleaseStatus(requestedStatus);

                //배송객체 '배송시작'상태로 생성
                // 출고 완료 상태일 경우, 배송 객체 생성
                Delivery delivery = deliveryRepository.save(Delivery.builder()
                        .deliveryStatus(deliveryStatusRepository.findByStatusName(DeliveryStatusCode.SHIPPED))
                        .shipmentNumber(makeShipNumber())
                        .deliveryFee(deliveryFee)
                        .build());

                //출고 완료 상태일 경우, 포장 객체 생성
                packagingRepository.save(Packaging.builder()
                        .release(targetRelease)
                        .orderDetail(targetOrderDetail)
                        .delivery(delivery)
                        .build());

                //출고가 완료되면, 배송 시작 -> 주문 상태는 '배송 시작'으로 변경
                presentOrderStatus = orderStatusRepository.findByStatusName(OrderStatusCode.SHIPPED);
                presentOrderStatusCode = OrderStatusCode.SHIPPED;
                if(!orderStatusPolicy.getOrderStatusTransitionRule().get(presentOrderStatus.getStatusName()).getRequiredPreviosConditionSet().contains(orderStatus)) {
                    throw new OrderException(ORDER_STATUS_TRANSITION_RULE_VIOLATION, HttpStatus.CONFLICT);
                }

                // 주문 상태 업데이트
                targetOrderDetail.changeOrderStatus(presentOrderStatus);

                for (ProductOrderEntity productOrderEntity : targetOrderDetail.getOrderList().getProductOrderEntityList()) {
                    productOrderEntity.changeStatus(presentOrderStatusCode);
                }
            }
            default -> new ReleaseException(INVALID_ACCESS, HttpStatus.CONFLICT);
        }

        // 명시적 저장
        orderDetailRepository.save(targetOrderDetail);
        releaseRepository.save(targetRelease);
    }

    /**
     * 고객의 출고상품 조회 서비스
     * @return 페이지 처리된 출고 상품 응답 객체
     */
    @Transactional
    public Page<ReleaseResponse.OfRetrieve> getReleaseDetailsByFilteredMembers(Long customerId, ReleaseStatusCode statusCode, String orderId, LocalDate startDeliveryDate, String recipient, String recipientPhoneNumber, String recipientAddress, String memberId, String memberName, String memberPhoneNumber, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        ResponseEntity<ApiResponse<Map<String, OrderResponse.MemberInfo>>> memberInfoMapResponse = null;
        Map<String, OrderResponse.MemberInfo> memberMap = null;
        boolean isAvailableMemberService = true;
        boolean isFilteredMember = false;

        if (memberName != null || memberPhoneNumber != null) {
            isFilteredMember = true;
            try {
                memberInfoMapResponse = memberServiceFeignClient.getFilterMemberMap(memberName, memberPhoneNumber);
                if (!memberInfoMapResponse.getStatusCode().is2xxSuccessful()) {
                    isAvailableMemberService = false;
                }
            } catch (FeignException e) {
                e.printStackTrace();
                return Page.empty(pageable);
            }
            if (!isAvailableMemberService || memberInfoMapResponse.getBody().getResult().isEmpty()) {
                // 필터링된 멤버 ID가 없으면 비어 있는 페이지 반환
                return Page.empty(pageable);
            }
            memberMap = memberInfoMapResponse.getBody().getResult();
        }

        Page<Release> releasesPage = releaseRepository.findReleases(customerId, statusCode, orderId, startDeliveryDate, recipient, recipientPhoneNumber, recipientAddress, memberId, isFilteredMember && isAvailableMemberService ? memberMap.values().stream().map(OrderResponse.MemberInfo::getMemberId).toList() : null, startDate, endDate, pageable);

        if (!isFilteredMember) {
            List<String> memberIds = releasesPage.getContent().stream().map(release -> release.getOrderDetail().getMemberId()).toList();
            try {
                memberInfoMapResponse = memberServiceFeignClient.getOrderMemberInfo(memberIds);
                if (!memberInfoMapResponse.getStatusCode().is2xxSuccessful()) {
                    isAvailableMemberService = false;
                } else {
                    memberMap = memberInfoMapResponse.getBody().getResult();
                }
            } catch (FeignException e) {
                e.printStackTrace();
                isAvailableMemberService = false;
            }
        }

        List<ReleaseResponse.OfRetrieve> filteredReleases = new ArrayList<>();

        for (Release release : releasesPage.getContent()) {
            OrderDetail orderDetail = release.getOrderDetail();
            OrderResponse.MemberInfo memberInfo = null;
            if (isAvailableMemberService && memberMap != null) {
                memberInfo = memberMap.get(orderDetail.getMemberId());
            }
            ReleaseResponse.OfRetrieve retrievedRelease = ReleaseResponse.OfRetrieve.convertedBy(orderDetail, release, memberInfo);
            filteredReleases.add(retrievedRelease);
        }

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
            throw new OrderException(ORDER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        targetRelease.changeStartDeliveryDate(updateDeliveryDate.getStartDeliveryDate());
        releaseRepository.save(targetRelease);
    }

    /**
     * 출고 메모 작성
     * @param updateMemo 출고 메모 작성 DTO
     * @throws OrderException 존재하지 않는 주문 ID인 경우
     */
    @Transactional
    public void changeReleaseMemo (Long customerId, ReleaseRequest.OfRegisterMemo updateMemo) {

        Release targetRelease = releaseRepository.findByOrderDetailId(updateMemo.getOrderId(), customerId);

        if (targetRelease == null) {
            throw new OrderException(ORDER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        targetRelease.changeReleaseMemo(updateMemo.getMemo());
        releaseRepository.save(targetRelease);
    }

    /**
     * 출고 보류 사유 메모 작성 (출고 상태가 '출고 보류'일 경우만 작성 가능)
     * @throws OrderException 존재하지 않는 주문 ID인 경우
     * @throws ReleaseException 출고 상태가 '출고 보류'가 아닌 경우
     * @param updateHoldMemo 출고 보류 메모 작성 DTO
     */
    @Transactional
    public void changeReleaseHoldMemo(Long customerId, ReleaseRequest.OfHoldMemo updateHoldMemo) {

        Release targetRelease = releaseRepository.findByOrderDetailId(updateHoldMemo.getOrderId(), customerId);

        if (targetRelease == null) {
            throw new OrderException(ORDER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        if(targetRelease.getReleaseStatus().getStatusName() != ReleaseStatusCode.HOLD_RELEASE){
            throw new ReleaseException(INVALID_RELEASE_STATUS_CODE, HttpStatus.CONFLICT);
        }

        targetRelease.changeReleaseHoldReason(updateHoldMemo.getMemo());
        releaseRepository.save(targetRelease);
    }

    /**
     * 상품의 출고 상태 일괄 수정 (출고 대기 -> 출고 보류, 출고 대기 -> 출고 완료, 출고 보류 -> 출고 완료)
     * @param bulkUpdateStatus (업데이틀 될 여러 주문 ID 들, 업데이트 될 출고 상태값) DTO
     * @throws OrderException 존재하지 않는 주문 ID인 경우
     * @throws ReleaseException 출고 상태 트랜지션 룰 위반일 경우
     * @throws OrderException 주문 상태 트랜지션 룰 위반일 경우
     * @return
     */
    @Transactional
    public void changeBulkReleaseStatus(Long customerId, ReleaseRequest.OfBulkUpdateReleaseStatus bulkUpdateStatus) {
        // 요청된 모든 주문 상세 정보를 가져옴
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByIdAndCustomerId(bulkUpdateStatus.getOrderIds(), customerId);

        // 요청된 ID 수와 조회된 결과 수가 다르면 존재하지 않는 ID가 있다는 의미
        if (orderDetails.size() != bulkUpdateStatus.getOrderIds().size()) {
            throw new OrderException(ORDER_ID_NOT_FOUND, HttpStatus.NOT_FOUND);
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
                throw new ReleaseException(RELEASE_STATUS_TRANSITION_RULE_VIOLATION, HttpStatus.CONFLICT);
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
     * @throws ReleaseException 배송일을 입력하지 않고, 출고 완료를 하려는 경우
     * @throws ReleaseException 잘못된 출고 상태 코드인 경우
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
                        throw new ReleaseException(DELIVERY_DATE_REQUIRED, HttpStatus.CONFLICT);
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
                throw new ReleaseException(INVALID_ACCESS, HttpStatus.CONFLICT);
        }
        orderDetailRepository.save(orderDetail);
    }

    /**
     * 고객의 합포장 신청
     * @param bulkUpdateStatus (업데이틀 될 여러 주문 ID 들, 업데이트 될 출고 상태값) DTO
     * @throws OrderException 존재하지 않는 주문 ID인 경우
     * @throws ReleaseException 출고 상태가 합포장완료가 아닌 경우
     * @throws ReleaseException 출고 상태 트랜지션 룰 위반일 경우
     * @throws ReleaseException 출고 상품들의 정보가 모두 일치하지 않은 경우
     * @return
     */
    @Transactional
    public void changeCombinedPackaging(Long customerId, ReleaseRequest.OfBulkUpdateReleaseStatus bulkUpdateStatus) {
        // 요청된 모든 주문 상세 정보를 가져옴
        List<OrderDetail> orderDetails = orderDetailRepository.findAllByIdAndCustomerId(bulkUpdateStatus.getOrderIds(), customerId);

        // 요청된 ID 수와 조회된 결과 수가 다르면 존재하지 않는 ID가 있다는 의미
        if (orderDetails.size() != bulkUpdateStatus.getOrderIds().size()) {
            throw new OrderException(ORDER_ID_NOT_FOUND, HttpStatus.NOT_FOUND);
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
            throw new ReleaseException(INVALID_RELEASE_STATUS_CODE, HttpStatus.CONFLICT);
        }

        if(isUniformOrder){
            // 모든 주문에 대해 상태 변경 수행
            for (OrderDetail orderDetail : orderDetails) {
                Release currentRelease = releaseRepository.findByOrderDetailId(orderDetail.getOrderDetailId(), customerId);
                ReleaseStatusCode currentReleaseStatus =  currentRelease.getReleaseStatus().getStatusName();

                // 출고 상태 전환 규칙 확인
                if (!releaseStatusPolicy.getReleaseStatusTransitionRule().get(requestedStatusCode).getRequiredPreviosConditionSet().contains(currentReleaseStatus)) {
                    throw new ReleaseException(RELEASE_STATUS_TRANSITION_RULE_VIOLATION, HttpStatus.CONFLICT);
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
            throw new ReleaseException(UNIFORM_ORDER_VIOLATION, HttpStatus.CONFLICT);
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
