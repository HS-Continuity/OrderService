package com.yeonieum.orderservice.domain.order.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.QOrderDetail;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class OrderDetailRepositoryImpl implements OrderDetailRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderDetail> findOrders(Long customerId, OrderStatusCode orderStatusCode, String orderDetailId, LocalDateTime orderDateTime, String recipient, String recipientPhoneNumber, String recipientAddress, String memberId, List<String> memberIds, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        QOrderDetail orderDetail = QOrderDetail.orderDetail;
        BooleanBuilder builder = new BooleanBuilder();

        if (customerId != null) {
            builder.and(orderDetail.customerId.eq(customerId));
        }
        if (orderStatusCode != null) {
            builder.and(orderDetail.orderStatus.statusName.eq(orderStatusCode));
        }
        if (orderDetailId != null) {
            builder.and(orderDetail.orderDetailId.contains(orderDetailId));
        }
        if (orderDateTime != null) {
            builder.and(orderDetail.orderDateTime.eq(orderDateTime));
        }
        if (recipient != null) {
            builder.and(orderDetail.recipient.contains(recipient));
        }
        if (recipientPhoneNumber != null) {
            builder.and(orderDetail.recipientPhoneNumber.contains(recipientPhoneNumber));
        }
        if (recipientAddress != null) {
            builder.and(orderDetail.deliveryAddress.contains(recipientAddress));
        }
        if (memberId != null) {
            builder.and(orderDetail.memberId.contains(memberId));
        }
        if (memberIds != null && !memberIds.isEmpty()) {
            builder.and(orderDetail.memberId.in(memberIds));
        }
        if (startDate != null) {
            builder.and(orderDetail.createdDate.goe(startDate));
        }
        if (endDate != null) {
            builder.and(orderDetail.createdDate.loe(endDate));
        }

        List<OrderDetail> orderDetails = queryFactory
                .selectFrom(orderDetail)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(orderDetail)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(orderDetails, pageable, total);
    }
}
