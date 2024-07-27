package com.yeonieum.orderservice.domain.order.repository;

import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface OrderDetailRepositoryCustom {
    Page<OrderDetail> findOrders(Long customerId, OrderStatusCode orderStatusCode, String orderDetailId, LocalDateTime orderDateTime, String recipient, String recipientPhoneNumber, String recipientAddress, String memberId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
