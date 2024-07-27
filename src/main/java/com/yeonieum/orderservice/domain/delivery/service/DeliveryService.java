package com.yeonieum.orderservice.domain.delivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.domain.combinedpackaging.repository.PackagingRepository;
import com.yeonieum.orderservice.domain.delivery.dto.DeliveryGenuineResponse;
import com.yeonieum.orderservice.domain.delivery.dto.DeliveryResponse;
import com.yeonieum.orderservice.domain.delivery.dto.DeliverySummaryResponse;
import com.yeonieum.orderservice.domain.delivery.repository.DeliveryStatusRepository;
import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final PackagingRepository packagingRepository;
    private final ObjectMapper objectMapper;
    private final DeliveryStatusRepository deliveryStatusRepository;

    /**
     * 고객의 배송 리스트를 조회하는 서비스
     * @param customerId 고객 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 페이징된 배송 응답 리스트
     */
    @Transactional(readOnly = true)
    public Page<DeliveryGenuineResponse> retrieveDeliveryList(Long customerId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Object[]> rawResults = packagingRepository.findAllDeliveryInfo(customerId, startDate, endDate, pageable);

        List<DeliveryGenuineResponse> deliveryResponses = rawResults.getContent().stream()
                .map(this::convertToDeliveryGenuineResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(deliveryResponses, pageable, rawResults.getTotalElements());
    }

    /**
     * 배송 정보를 객체로 변환하는 메서드
     * @param result 쿼리 결과 배열
     * @return 변환된 배송 응답 객체
     */
    private DeliveryGenuineResponse convertToDeliveryGenuineResponse(Object[] result) {
        DeliveryResponse deliveryResponse = DeliveryResponse.convertedBy(result);
        DeliveryStatusCode deliveryStatusCode = deliveryStatusRepository.findById(deliveryResponse.getDeliveryStatusCode())
                .map(status -> status.getStatusName())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 배송상태 코드 입니다."));

        return DeliveryGenuineResponse.convertedBy(deliveryResponse, deliveryStatusCode, objectMapper);
    }

    /**
     * 배송 상태별 카운팅
     * @param customerId
     * @return 배송 상태별 카운팅된 수
     */
    public List<DeliverySummaryResponse> countDeliveryStatus(Long customerId) {
        return packagingRepository.countByDeliveryStatus(customerId);
    }
}
