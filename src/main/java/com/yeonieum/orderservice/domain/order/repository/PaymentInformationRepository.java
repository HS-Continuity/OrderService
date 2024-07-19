package com.yeonieum.orderservice.domain.order.repository;

import com.yeonieum.orderservice.domain.order.entity.PaymentInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentInformationRepository extends JpaRepository<PaymentInformation,Long> {
    PaymentInformation findByOrderDetail_OrderDetailId(String orderDetailId);
}
