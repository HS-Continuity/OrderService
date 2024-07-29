package com.yeonieum.orderservice.domain.regularorder.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;
import com.yeonieum.orderservice.domain.regularorder.entity.QRegularDeliveryApplication;
import com.yeonieum.orderservice.domain.regularorder.entity.QRegularDeliveryReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RegularDeliveryReservationRepositoryCustomImpl implements RegularDeliveryReservationRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public List<RegularOrderResponse.OfRetrieveDailySummary> findRegularOrderCountsBetween(LocalDate startDate, LocalDate endDate, Long customerId) {
        QRegularDeliveryReservation reservation = QRegularDeliveryReservation.regularDeliveryReservation;
        QRegularDeliveryApplication application = QRegularDeliveryApplication.regularDeliveryApplication;
        BooleanExpression isBetweenMonth = reservation.startDate.between(startDate, endDate);
        BooleanExpression isCustomerId = application.customerId.eq(customerId);

        return queryFactory
                .select(Projections.constructor(RegularOrderResponse.OfRetrieveDailySummary.class,
                        reservation.regularDeliveryReservationId.count(),
                        application.mainProductId,
                        application.regularDeliveryApplicationId,
                        reservation.startDate))
                .from(reservation)
                .join(reservation.regularDeliveryApplication, application)
                .where(isBetweenMonth, isCustomerId)
                .groupBy(application.regularDeliveryApplicationId, reservation.startDate)
                .orderBy(reservation.startDate.asc())
                .fetch();
    }


    public List<RegularOrderResponse.OfRetrieveDailyDetail> findRegularOrderList(LocalDate date, Long customerId, int startPage, int pageSize) {
        QRegularDeliveryReservation reservation = QRegularDeliveryReservation.regularDeliveryReservation;
        QRegularDeliveryApplication application = QRegularDeliveryApplication.regularDeliveryApplication;
        BooleanExpression isDate = reservation.startDate.eq(date);
        BooleanExpression isCustomerId = application.customerId.eq(customerId);

        int offset = (startPage) * pageSize;
        return queryFactory
                .select(Projections.constructor(RegularOrderResponse.OfRetrieveDailyDetail.class,
                        application.regularDeliveryApplicationId,
                        reservation.startDate,
                        reservation.regularDeliveryReservationId.count(),
                        reservation.productId))
                .from(reservation)
                .join(reservation.regularDeliveryApplication, application)
                .where(isDate, isCustomerId)
                .orderBy(reservation.regularDeliveryReservationId.asc())
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }
}

