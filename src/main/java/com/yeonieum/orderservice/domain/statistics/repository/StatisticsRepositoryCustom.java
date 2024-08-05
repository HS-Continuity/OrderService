package com.yeonieum.orderservice.domain.statistics.repository;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderType;

import java.util.List;

public interface StatisticsRepositoryCustom {

    List<OrderResponse.ProductOrderCount> findAllProductsByCondition(Long customerId, Gender gender, Integer ageRange, OrderType orderType, Integer month);
}
