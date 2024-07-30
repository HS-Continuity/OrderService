package com.yeonieum.orderservice.domain.release.repository;

import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReleaseRepositoryCustom {
    Page<Release> findReleases(Long customerId, ReleaseStatusCode statusCode, String orderId, LocalDate startDeliveryDate, String recipient, String recipientPhoneNumber, String recipientAddress, String memberId, List<String> memberIds, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
