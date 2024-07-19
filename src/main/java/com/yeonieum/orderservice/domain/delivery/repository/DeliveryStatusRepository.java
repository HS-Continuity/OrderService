package com.yeonieum.orderservice.domain.delivery.repository;

import com.yeonieum.orderservice.domain.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryStatusRepository extends JpaRepository<DeliveryStatus, Long> {
}
