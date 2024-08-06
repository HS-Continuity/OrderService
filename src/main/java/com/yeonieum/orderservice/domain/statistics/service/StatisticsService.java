package com.yeonieum.orderservice.domain.statistics.service;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.statistics.repository.StatisticsRepository;
import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderType;
import java.time.LocalDate;
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

    public List<OrderResponse.ProductMonthlySales> getTop5ProductsMonthlySales(Long customerId, int months) {
        return statisticsRepository.findTop5ProductsMonthlySales(customerId, months);
    }

    public List<OrderResponse.MonthlyRevenue> getMonthlyRevenue(Long customerId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        return statisticsRepository.findMonthlyRevenue(customerId, startDate, endDate);
    }

    public List<OrderResponse.MemberGrowth> getMemberGrowthCumulative(Long customerId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        List<OrderResponse.MemberGrowth> memberGrowths = statisticsRepository.findMemberGrowth(customerId, startDate, endDate);
        long cumulativeMemberCount = 0;
        for (OrderResponse.MemberGrowth growth : memberGrowths) {
            cumulativeMemberCount += growth.getMemberCount();
            growth.setMemberCount(cumulativeMemberCount);
        }
        return memberGrowths;
    }
}
