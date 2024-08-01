package com.yeonieum.orderservice.domain.statistics.service;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.statistics.repository.StatisticsRepository;
import com.yeonieum.orderservice.global.enums.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public List<OrderResponse.ProductOrderCount> genderProductOrderCounts (Long customerId, Gender gender) {

        List<OrderResponse.ProductOrderCount> productOrderCounts = statisticsRepository.findTop3ProductsByGender(customerId, gender);
        return productOrderCounts;
    }

    public List<OrderResponse.ProductOrderCount> ageProductOrderCounts (Long customerId, int ageRange) {

        List<OrderResponse.ProductOrderCount> productOrderCounts = statisticsRepository.findTop3ProductsByAgeRange(customerId, ageRange);
        return productOrderCounts;
    }
}
