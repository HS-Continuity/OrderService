package com.yeonieum.orderservice.domain.statistics.repository;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderType;
import java.time.LocalDate;

import java.util.List;

public interface StatisticsRepositoryCustom {

    List<OrderResponse.ProductOrderCount> findAllProductsByCondition(Long customerId, Gender gender, Integer ageRange, OrderType orderType, Integer month);
    List<OrderResponse.ProductMonthlySales> findTop5ProductsMonthlySales(Long customerId, int months);
    List<OrderResponse.MonthlyRevenue> findMonthlyRevenue(Long customerId, LocalDate startDate, LocalDate endDate);
    List<OrderResponse.MemberGrowth> findMemberGrowth(Long customerId, LocalDate startDate, LocalDate endDate);
}
