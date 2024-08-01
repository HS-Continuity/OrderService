package com.yeonieum.orderservice.domain.statistics.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.statistics.entity.QStatistics;
import com.yeonieum.orderservice.global.enums.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class StatisticsRepositoryImpl implements StatisticsRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<OrderResponse.ProductOrderCount> findTop3ProductsByGender(Long customerId, Gender gender) {
        QStatistics statistics = QStatistics.statistics;

        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        return queryFactory
                .select(Projections.constructor(OrderResponse.ProductOrderCount.class,
                        statistics.productId,
                        statistics.productId.count().as("orderCount")))
                .from(statistics)
                .where(statistics.customerId.eq(customerId)
                        .and(statistics.gender.eq(gender))
                        .and(statistics.purchaseDate.after(threeMonthsAgo)))
                .groupBy(statistics.productId)
                .orderBy(statistics.productId.count().desc())
                .limit(3)
                .fetch();
    }

    @Override
    public List<OrderResponse.ProductOrderCount> findTop3ProductsByAgeRange(Long customerId, int ageRange) {
        QStatistics statistics = QStatistics.statistics;

        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        return queryFactory
                .select(Projections.constructor(OrderResponse.ProductOrderCount.class,
                        statistics.productId,
                        statistics.productId.count().as("orderCount")))
                .from(statistics)
                .where(statistics.customerId.eq(customerId)
                        .and(statistics.ageRange.eq(ageRange))
                        .and(statistics.purchaseDate.after(threeMonthsAgo)))
                .groupBy(statistics.productId)
                .orderBy(statistics.productId.count().desc())
                .limit(3)
                .fetch();
    }
}
