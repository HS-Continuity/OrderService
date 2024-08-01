package com.yeonieum.orderservice.domain.statistics.service;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.statistics.repository.StatisticsRepository;
import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    /**
     * 고객의 상품 성별 TOP3
     * @param customerId
     * @param gender
     */
    public List<OrderResponse.ProductOrderCount> genderProductOrderCounts (Long customerId, Gender gender) {

        List<OrderResponse.ProductOrderCount> productOrderCounts = statisticsRepository.findTop3ProductsByGender(customerId, gender);
        return productOrderCounts;
    }

    /**
     * 고객의 상품 연령별 TOP3
     * @param customerId
     * @param ageRange
     */
    public List<OrderResponse.ProductOrderCount> ageProductOrderCounts (Long customerId, int ageRange) {

        List<OrderResponse.ProductOrderCount> productOrderCounts = statisticsRepository.findTop3ProductsByAgeRange(customerId, ageRange);
        return productOrderCounts;
    }

    /**
     * 고객의 판매타입별 상품 판매량
     * @param customerId
     * @param orderType
     */
    public List<OrderResponse.ProductOrderCount> orderTypeProductOrderCounts (Long customerId, OrderType orderType) {

        List<OrderResponse.ProductOrderCount> productOrderCounts = statisticsRepository.findAllProductsByOrderType(customerId, orderType);
        return productOrderCounts;
    }
}
