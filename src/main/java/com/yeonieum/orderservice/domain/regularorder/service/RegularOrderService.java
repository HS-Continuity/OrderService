package com.yeonieum.orderservice.domain.regularorder.service;

import com.yeonieum.orderservice.domain.regularorder.dto.request.RegularOrderRequest;
import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplication;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryReservation;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryStatus;
import com.yeonieum.orderservice.domain.regularorder.repository.RegularDeliveryApplicationDayRepository;
import com.yeonieum.orderservice.domain.regularorder.repository.RegularDeliveryApplicationRepository;
import com.yeonieum.orderservice.domain.regularorder.repository.RegularDeliveryReservationRepository;
import com.yeonieum.orderservice.domain.regularorder.repository.RegularDeliveryStatusRepository;
import com.yeonieum.orderservice.global.enums.DayOfWeek;
import com.yeonieum.orderservice.global.enums.RegularDeliveryStatusCode;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.infrastructure.feignclient.ProductServiceFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


/**
 * 1. 정기주문 생성
 * 2. 정기주문 내역 조회
 * 3. 주문생성 예약
 * 4. 정기주문회차 미루기(취소)
 * 5. 정기주문 취소
 */
@Service
@RequiredArgsConstructor
public class RegularOrderService {
    private final RegularDeliveryApplicationRepository regularDeliveryApplicationRepository;
    private final RegularDeliveryReservationRepository regularDeliveryReservationRepository;
    private final RegularDeliveryStatusRepository regularDeliveryStatusRepository;
    private final RegularDeliveryApplicationDayRepository regularDeliveryApplicationDayRepository;
    private final ProductServiceFeignClient feignClient;

    /**
     * 정기배송신청 생성 및 정기배송예약 드르륵 생성
     * 스케쥴 작업 등록하기
     * @param creationRequest
     */
    @Transactional
    public void subscriptionDelivery(RegularOrderRequest.OfCreation creationRequest) {
        RegularDeliveryApplication regularDeliveryApplication = creationRequest.toApplicationEntity();
        regularDeliveryApplicationRepository.save(regularDeliveryApplication);
        regularDeliveryApplicationDayRepository.saveAll(creationRequest.toApplicationDayEnityList(regularDeliveryApplication));

        Set<LocalDate> deliveryDateSet = calculateDeliveryDates(creationRequest.getDeliveryPeriod());

        RegularDeliveryStatus status =
                regularDeliveryStatusRepository.findByStatusName(RegularDeliveryStatusCode.PENDING.getCode());
        List<RegularDeliveryReservation> regularDeliveryReservationList = creationRequest.toReservationEntityList(deliveryDateSet, regularDeliveryApplication, status);
        regularDeliveryReservationRepository.saveAll(regularDeliveryReservationList);
    }

    /**
     * 회원의 정기주문 리스트 조회
     * @param memberId
     * @param pageable
     * @return
     */
    @Transactional
    public Page<RegularOrderResponse.OfRetrieve> retrieveRegularDeliveryList(String memberId, Pageable pageable) {
        Page<RegularDeliveryApplication> applicationList = regularDeliveryApplicationRepository.findByMemberIdOrderByCreatedAtAsc(memberId, pageable);
        List<Long> productIdList = applicationList.map(application -> application.getMainProductId()).stream().collect(Collectors.toList());
        ResponseEntity response = feignClient.bulkRetrieveProductInformation(productIdList);
        Map<Long, RegularOrderResponse.ProductOrder> productOrderMap =
                (Map<Long, RegularOrderResponse.ProductOrder>) ((ApiResponse) response.getBody()).getResult();

        return applicationList.map(application ->
                RegularOrderResponse.OfRetrieve.convertedBy(application, productOrderMap));
    }

    /**
     * 회원의 정기주문 상세조회(상품 리스트, 배송정보, 다음배송일 정보 노출)
     * @param regularDeliveryApplicationId
     * @return
     */
    @Transactional
    public RegularOrderResponse.OfRetrieveDetails retrieveRegularDeliveryDetails(Long regularDeliveryApplicationId) {
        RegularDeliveryApplication application = regularDeliveryApplicationRepository.findWithReservationsAndApplicationDaysById(regularDeliveryApplicationId);
        List<Long> productIdList = application.getRegularDeliveryReservationList().stream().map(reservation
                -> reservation.getProductId()).collect(Collectors.toList());
        ResponseEntity response = feignClient.bulkRetrieveProductInformation(productIdList);
        Map<Long, RegularOrderResponse.ProductOrder> productOrderMap =
                (Map<Long, RegularOrderResponse.ProductOrder>) ((ApiResponse) response.getBody()).getResult();

        return RegularOrderResponse.OfRetrieveDetails.convertedBy(application, productOrderMap);
    }


    /**
     * 정기주문 예약 취소
     * @param regularDeliveryApplicationId
     */
    @Transactional
    public void cancelRegularDelivery(Long regularDeliveryApplicationId) {
        RegularDeliveryStatus status = regularDeliveryStatusRepository.findByStatusName(RegularDeliveryStatusCode.CANCELED.getCode());
        RegularDeliveryApplication application = regularDeliveryApplicationRepository.findByIdWithPendingReservations(regularDeliveryApplicationId);
        application.changeDeliveryStatus(status);
        for(RegularDeliveryReservation deliveryReservation : application.getRegularDeliveryReservationList()) {
            deliveryReservation.changeStatus(status);
        }
    }


    /**
     * 정기주문 회차 미루기
     * @param regularOrderApplicationId
     * @param postPoneRequest
     */
    @Transactional
    public void skipRegularDeliveryReservation (Long regularOrderApplicationId, RegularOrderRequest.OfPostPone postPoneRequest) {
        RegularDeliveryReservation regularDeliveryReservation =
                regularDeliveryReservationRepository.findByDeliveryApplicationAndProductId(regularOrderApplicationId, postPoneRequest.getProductId());

        RegularDeliveryStatus status = regularDeliveryStatusRepository.findByStatusName(RegularDeliveryStatusCode.POSTPONED.getCode());
        regularDeliveryReservation.changeStatus(status);
        regularDeliveryReservation.getRegularDeliveryApplication().changeCompletedRounds(regularDeliveryReservation.getDeliveryRounds());
    }

    /**
     * 배송주기에 따른 배송일 계산
     * @param deliveryPeriod
     * @return
     */
    public Set<LocalDate> calculateDeliveryDates (RegularOrderRequest.DeliveryPeriod deliveryPeriod) {
        LocalDate startDate = deliveryPeriod.getStartDate();
        LocalDate endDate = deliveryPeriod.getEndDate();
        List<DayOfWeek> dayOfWeeks = deliveryPeriod.getDeliveryDayOfWeeks();
        int cycleWeeks = deliveryPeriod.getDeliveryCycle();


        LocalDate nextDeliveryDate = startDate;
        Set<LocalDate> deliveryDateSet = new TreeSet<>();
        while(!nextDeliveryDate.isAfter(endDate)) {
            for (DayOfWeek deliveryDay : dayOfWeeks) {
                LocalDate deliveryDate = nextDeliveryDate.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.valueOf(deliveryDay.getStoredDayValue())));
                if (!deliveryDate.isAfter(endDate)) {
                    deliveryDateSet.add(deliveryDate);
                }
            }
            nextDeliveryDate = nextDeliveryDate.plusWeeks(cycleWeeks);
        }

        return deliveryDateSet;
    }

}
