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

    public List<RegularOrderResponse.OfRetrieveDailyCount> findRegularOrderCountsBetween(LocalDate startDate, LocalDate endDate, Long customerId) {
        QRegularDeliveryReservation reservation = QRegularDeliveryReservation.regularDeliveryReservation;
        QRegularDeliveryApplication application = QRegularDeliveryApplication.regularDeliveryApplication;
        BooleanExpression isBetweenMonth = reservation.startDate.between(startDate, endDate);
        BooleanExpression isCustomerId = application.customerId.eq(customerId);

        return queryFactory
                .select(Projections.constructor(RegularOrderResponse.OfRetrieveDailyCount.class,
                        application.regularDeliveryApplicationId,
                        reservation.startDate,
                        reservation.regularDeliveryReservationId.count(),
                        application.regularDeliveryApplicationId))
                .from(reservation)
                .join(reservation.regularDeliveryApplication, application)
                .where(isBetweenMonth, isCustomerId)
                .groupBy(application.regularDeliveryApplicationId, reservation.startDate)
                .orderBy(reservation.startDate.asc())
                .fetch();
    }
}

