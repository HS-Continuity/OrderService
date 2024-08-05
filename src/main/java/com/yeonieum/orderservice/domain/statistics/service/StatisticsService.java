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
     * 고객의 상품 통계 판매량
     * @param customerId
     * @param gender
     * @param ageRange
     * @param orderType
     * @param month
     */
    public List<OrderResponse.ProductOrderCount> productOrderCounts (Long customerId, Gender gender, Integer ageRange, OrderType orderType, Integer month) {

        List<OrderResponse.ProductOrderCount> productOrderCounts = statisticsRepository.findAllProductsByCondition(customerId, gender, ageRange, orderType, month);
        return productOrderCounts;
    }
}
