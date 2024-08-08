package com.yeonieum.orderservice.domain.statistics.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.statistics.entity.QStatistics;
import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class StatisticsRepositoryImpl implements StatisticsRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<OrderResponse.ProductOrderCount> findAllProductsByCondition(Long customerId, Gender gender, Integer ageRange, OrderType orderType, Integer month) {
        QStatistics statistics = QStatistics.statistics;

        LocalDate targetDate = LocalDate.now().minusMonths(month != null ? month : 3); // month가 null이면 기본값으로 3개월 전을 사용합니다.

        BooleanBuilder whereClause = new BooleanBuilder();
        whereClause.and(statistics.customerId.eq(customerId));
        whereClause.and(statistics.purchaseDate.after(targetDate));

        Integer limit = 0;

        if (gender != null) {
            whereClause.and(statistics.gender.eq(gender));
            limit = 3;
        }
        if (ageRange != null) {
            whereClause.and(statistics.ageRange.eq(ageRange));
            limit = 2;
        }
        if (orderType != null) {
            whereClause.and(statistics.orderType.eq(orderType));
            limit = 6;
        }

        JPAQuery<OrderResponse.ProductOrderCount> query = queryFactory
                .select(Projections.constructor(OrderResponse.ProductOrderCount.class,
                        statistics.productId,
                        statistics.productId.count().as("orderCount")))
                .from(statistics)
                .where(whereClause)
                .groupBy(statistics.productId)
                .orderBy(statistics.productId.count().desc());

        if (limit > 0) {
            query.limit(limit);
        }

        return query.fetch();
    }

    @Override
    public List<OrderResponse.ProductMonthlySales> findTop5ProductsMonthlySales(Long customerId, int months) {
        QStatistics statistics = QStatistics.statistics;

        LocalDate startDate = LocalDate.now().minusMonths(months).withDayOfMonth(1);

        // 상위 5개 제품 ID 조회
        List<Long> top5ProductIds = queryFactory
                .select(statistics.productId)
                .from(statistics)
                .where(statistics.customerId.eq(customerId)
                        .and(statistics.purchaseDate.goe(startDate)))
                .groupBy(statistics.productId)
                .orderBy(statistics.quantity.sum().desc())
                .limit(5)
                .fetch();

        // 상위 5개 제품의 이름과 월별 판매량을 조회
        return queryFactory
                .select(Projections.constructor(OrderResponse.ProductMonthlySales.class,
                        statistics.productId,
                        statistics.purchaseDate.year(),
                        statistics.purchaseDate.month(),
                        statistics.quantity.sum().longValue()))
                .from(statistics)
                .where(statistics.customerId.eq(customerId)
                        .and(statistics.purchaseDate.goe(startDate))
                        .and(statistics.productId.in(top5ProductIds)))
                .groupBy(statistics.productId, statistics.purchaseDate.year(), statistics.purchaseDate.month())
                .orderBy(statistics.productId.asc(), statistics.purchaseDate.year().asc(), statistics.purchaseDate.month().asc())
                .fetch();
    }


    public List<OrderResponse.MonthlyRevenue> findMonthlyRevenue(Long customerId, LocalDate startDate, LocalDate endDate) {
        QStatistics statistics = QStatistics.statistics;

        return queryFactory
                .select(Projections.constructor(OrderResponse.MonthlyRevenue.class,
                        statistics.purchaseDate.year(),
                        statistics.purchaseDate.month(),
                        statistics.price.multiply(statistics.quantity).sum().longValue()))
                .from(statistics)
                .where(statistics.customerId.eq(customerId)
                        .and(statistics.purchaseDate.between(startDate, endDate)))
                .groupBy(statistics.purchaseDate.year(), statistics.purchaseDate.month())
                .orderBy(statistics.purchaseDate.year().asc(), statistics.purchaseDate.month().asc())
                .fetch();
    }


    public List<OrderResponse.MemberGrowth> findMemberGrowth(Long customerId, LocalDate startDate, LocalDate endDate) {
        QStatistics statistics = QStatistics.statistics;

        return queryFactory
                .select(Projections.constructor(OrderResponse.MemberGrowth.class,
                        statistics.purchaseDate.year(),
                        statistics.purchaseDate.month(),
                        statistics.memberId.countDistinct().longValue()))
                .from(statistics)
                .where(statistics.customerId.eq(customerId)
                        .and(statistics.purchaseDate.between(startDate, endDate)))
                .groupBy(statistics.purchaseDate.year(), statistics.purchaseDate.month())
                .orderBy(statistics.purchaseDate.year().asc(), statistics.purchaseDate.month().asc())
                .fetch();
    }
}
