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
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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

    @Transactional
    public List<RegularOrderResponse.OfRetrieveDailyCount> retrieveRegularOrderCountsBetween(LocalDate startDate, LocalDate endDate, Long customerId) {
        List<RegularOrderResponse.OfRetrieveDailyCount> regularOrderCountsForMonth = regularDeliveryReservationRepository.findRegularOrderCountsBetween(startDate, endDate, customerId);

        ResponseEntity<ApiResponse<Map<Long, RegularOrderResponse.ProductOrder>>> response = null;
        Map<Long, RegularOrderResponse.ProductOrder> productOrderMap = null;

        List<Long> productIdList = regularOrderCountsForMonth.stream().map(dailyOrderCount -> dailyOrderCount.getProductId()).collect(Collectors.toList());
        boolean isAvailableProductService = true;
        try {
            response = feignClient.bulkRetrieveProductInformation(productIdList);
            isAvailableProductService = response.getStatusCode().is2xxSuccessful() ? true : false;
        } catch (FeignException e) {
            e.printStackTrace();
            isAvailableProductService = false;
        }

        // 받아온 응답을 바탕으로 상품명 바인딩
        for(RegularOrderResponse.OfRetrieveDailyCount dailyOrderCount : regularOrderCountsForMonth) {
            productOrderMap = response.getBody().getResult();
            String productName = isAvailableProductService ? productOrderMap.get(dailyOrderCount.getProductId()).getProductName() : null;
            dailyOrderCount.bindProductName(productName);
            dailyOrderCount.setAvailableProductService(isAvailableProductService);
        }

        return regularOrderCountsForMonth;
    }

    /**
     * 정기배송신청 생성 및 정기배송예약 모두 생성
     * 스케쥴 작업 등록하기
     * @param creationRequest
     */
    @Transactional
    public Long subscriptionDelivery(RegularOrderRequest.OfCreation creationRequest) {
        RegularDeliveryStatus pending = regularDeliveryStatusRepository.findByStatusName(RegularDeliveryStatusCode.PENDING.getCode());
        RegularDeliveryApplication regularDeliveryApplication = creationRequest.toApplicationEntity(pending);
        RegularDeliveryApplication savedEntity = regularDeliveryApplicationRepository.save(regularDeliveryApplication);
        regularDeliveryApplicationDayRepository.saveAll(creationRequest.toApplicationDayEnityList(regularDeliveryApplication));
        // 총 배송 회차
        Set<LocalDate> deliveryDateSet = calculateDeliveryDates(creationRequest.getDeliveryPeriod());
        regularDeliveryApplication.changeTotalDeliveryRounds(deliveryDateSet.size());

        LocalDate firstDeliveryDate = deliveryDateSet.iterator().next();
        regularDeliveryApplication.changeNextDeliveryDate(firstDeliveryDate);
        regularDeliveryApplication.changeStartDate(firstDeliveryDate);
        regularDeliveryApplicationRepository.save(regularDeliveryApplication);

        regularDeliveryReservationRepository.saveAll(creationRequest.toReservationEntityList(deliveryDateSet, regularDeliveryApplication, pending));
        return savedEntity.getRegularDeliveryApplicationId();
    }

    /**
     * 회원의 정기주문 리스트 조회
     * @param memberId
     * @param pageable
     * @return
     */
    @Transactional
    public Page<RegularOrderResponse.OfRetrieve> retrieveRegularDeliveryList(String memberId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<RegularDeliveryApplication> applicationList = regularDeliveryApplicationRepository.findByMemberIdAndCreatedDate(memberId, startDate, endDate, pageable);
        ResponseEntity<ApiResponse<Map<Long, RegularOrderResponse.ProductOrder>>> response = null;

        // 상품Id 리스트 추출 후 상품서비스의 상품정보 조회 API 호출
        List<Long> productIdList = applicationList.map(application -> application.getMainProductId()).stream().collect(Collectors.toList());
        boolean isAvailableProductService = true;
        try {
            response = feignClient.bulkRetrieveProductInformation(productIdList);
            isAvailableProductService = response.getStatusCode().is2xxSuccessful() ? true : false;
        } catch (FeignException e) {
            e.printStackTrace();
            isAvailableProductService = false;
        }

        final boolean finalIsAvailableProductService = isAvailableProductService;
        final Map<Long, RegularOrderResponse.ProductOrder> productOrderMap = isAvailableProductService ? response.getBody().getResult() : null;
        Map<Long, RegularOrderResponse.ProductOrder> finalProductOrderMap = productOrderMap;
        applicationList.stream().forEach(application -> application.getRegularDeliveryReservationList().stream().forEach(reservation -> {
            finalProductOrderMap.get(reservation.getProductId()).changeProductAmount(reservation.getQuantity());
        }));
        return applicationList.map(application -> RegularOrderResponse.OfRetrieve.convertedBy(application, productOrderMap, finalIsAvailableProductService));
    }

    /**
     * 회원의 정기주문 상세조회(상품 리스트, 배송정보, 다음배송일 정보 노출)
     * @param regularDeliveryApplicationId
     * @return
     */
    @Transactional
    public RegularOrderResponse.OfRetrieveDetails retrieveRegularDeliveryDetails(Long regularDeliveryApplicationId) {
        RegularDeliveryApplication application = regularDeliveryApplicationRepository.findWithReservationsAndApplicationDaysById(regularDeliveryApplicationId);

        // 상품Id 리스트 추출 후 상품서비스의 상품정보 조회 API 호출
        List<Long> productIdList = application.getRegularDeliveryReservationList().stream().map(reservation -> reservation.getProductId()).collect(Collectors.toList());
        ResponseEntity<ApiResponse<Map<Long, RegularOrderResponse.ProductOrder>>> response = null;
        Map<Long, RegularOrderResponse.ProductOrder> productOrderMap = null;
        boolean isAvailableProductService = true;
        try {
            response = feignClient.bulkRetrieveProductInformation(productIdList);
            isAvailableProductService = response.getStatusCode().is2xxSuccessful() ? true : false;
        } catch (FeignException e) {
            e.printStackTrace();
            isAvailableProductService = false;
        }

        productOrderMap = isAvailableProductService ? response.getBody().getResult() : null;
        Map<Long, RegularOrderResponse.ProductOrder> finalProductOrderMap = productOrderMap;
        application.getRegularDeliveryReservationList().stream().forEach(reservation -> {
            finalProductOrderMap.get(reservation.getProductId()).changeProductAmount(reservation.getQuantity());
        });
        return RegularOrderResponse.OfRetrieveDetails.convertedBy(application, productOrderMap, isAvailableProductService);
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
        RegularDeliveryApplication application = regularDeliveryApplicationRepository.findById(regularOrderApplicationId).orElseThrow(
                () -> new IllegalArgumentException("해당 정기주문신청이 존재하지 않습니다.")
        );

        if(application.getCompletedRounds() == application.getTotalDeliveryRounds()) {
            throw new IllegalArgumentException("미룰 수 있는 정기배송예약이 존재하지 않습니다.");

        }

        List<RegularDeliveryReservation> regularDeliveryReservationList =
                regularDeliveryReservationRepository.findByDeliveryApplicationAndProductId(regularOrderApplicationId, postPoneRequest.getProductId(), application.getCompletedRounds()+1);


        // 다음 배송일 변경
        application.changeCompletedRounds(application.getCompletedRounds()+1);
        if(!(application.getCompletedRounds() == application.getTotalDeliveryRounds())) {
            List<RegularDeliveryReservation> nextRegularDeliveryReservationList =
                    regularDeliveryReservationRepository.findByDeliveryApplicationAndProductId(regularOrderApplicationId, postPoneRequest.getProductId(), application.getCompletedRounds()+1);
            application.changeNextDeliveryDate(nextRegularDeliveryReservationList.get(0).getStartDate());
        }


        regularDeliveryReservationList.forEach(reservation -> reservation.changeStatus(regularDeliveryStatusRepository.findByStatusName(RegularDeliveryStatusCode.POSTPONE.getCode())));
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


        LocalDate nextDeliveryDate = startDate.with(TemporalAdjusters.next(java.time.DayOfWeek.valueOf(dayOfWeeks.get(0).getStoredDayValue()))).plusDays(7);
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

    /**
     * 현재 날짜 및 시간과 UUID 4자리 조합으로 정기배송번호 생성
     *
     * @return
     */
    private String makeRegularDeliveryNumber() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return timestamp + "-" + uniqueId;
    }
}
