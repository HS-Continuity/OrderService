package com.yeonieum.orderservice.domain.release.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yeonieum.orderservice.domain.release.entity.QRelease;
import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ReleaseRepositoryImpl implements ReleaseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Release> findReleases(Long customerId, ReleaseStatusCode statusCode, String orderId, LocalDate startDeliveryDate, String recipient, String recipientPhoneNumber, String recipientAddress, String memberId, List<String> memberIds, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        QRelease release = QRelease.release;
        BooleanBuilder builder = new BooleanBuilder();

        if (customerId != null) {
            builder.and(release.orderDetail.customerId.eq(customerId));
        }
        if (statusCode != null) {
            builder.and(release.releaseStatus.statusName.eq(statusCode));
        }
        if (orderId != null) {
            builder.and(release.orderDetail.orderDetailId.contains(orderId));
        }
        if (startDeliveryDate != null) {
            builder.and(release.startDeliveryDate.eq(startDeliveryDate));
        }
        if (recipient != null) {
            builder.and(release.orderDetail.recipient.contains(recipient));
        }
        if (recipientPhoneNumber != null) {
            builder.and(release.orderDetail.recipientPhoneNumber.contains(recipientPhoneNumber));
        }
        if (recipientAddress != null) {
            builder.and(release.orderDetail.deliveryAddress.contains(recipientAddress));
        }
        if (memberId != null) {
            builder.and(release.orderDetail.memberId.contains(memberId));
        }
        if (memberIds != null && !memberIds.isEmpty()) {
            builder.and(release.orderDetail.memberId.in(memberIds));
        }
        if (startDate != null) {
            builder.and(release.createdDate.goe(startDate));
        }
        if (endDate != null) {
            builder.and(release.createdDate.loe(endDate));
        }

        List<Release> releases = queryFactory
                .selectFrom(release)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(release)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(releases, pageable, total);
    }
}
