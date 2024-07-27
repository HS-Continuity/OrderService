package com.yeonieum.orderservice.domain.delivery.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.domain.combinedpackaging.repository.PackagingRepository;
import com.yeonieum.orderservice.domain.delivery.dto.DeliveryGenuineResponse;
import com.yeonieum.orderservice.domain.delivery.dto.DeliveryResponse;
import com.yeonieum.orderservice.domain.delivery.dto.DeliverySummaryResponse;
import com.yeonieum.orderservice.domain.delivery.repository.DeliveryStatusRepository;
import com.yeonieum.orderservice.domain.order.entity.ProductOrderListEntityList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final PackagingRepository packagingRepository;
    private final ObjectMapper objectMapper;
    private final DeliveryStatusRepository deliveryStatusRepository;

    @Transactional
    public List<DeliveryGenuineResponse> retrieveDeliveryList(Long customerId) {
        List<Object[]> rawResults = packagingRepository.findAllDeliveryInfo(customerId);
        List<DeliveryResponse> deliveryResponses = rawResults.stream()
                .map(result -> DeliveryResponse.convertedBy(result))
                .collect(Collectors.toList());

        return deliveryResponses.stream().map(result -> {
            try {
                return DeliveryGenuineResponse.builder()
                        .deliveryId(result.getDeliveryId())
                        .shipmentNumber(result.getShipmentNumber())
                        .deliveryStatusCode(deliveryStatusRepository.findById(result.getDeliveryStatusCode()).get().getStatusName())
                        .representativeOrderId(result.getRepresentativeOrderId())
                        .additionalOrderCount(result.getAdditionalOrderCount())
                        .memberId(result.getMemberId())
                        .startDeliveryDate(result.getStartDeliveryDate().toLocalDate())
                        .productOrderListEntityLists(objectMapper.readValue("{\"productOrderListEntityList\":[" + result.getProductOrderListEntityList() + "]}", ProductOrderListEntityList.class))
                        .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
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
