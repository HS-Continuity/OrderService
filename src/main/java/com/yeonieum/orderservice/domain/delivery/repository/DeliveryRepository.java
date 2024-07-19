package com.yeonieum.orderservice.domain.delivery.repository;

import com.yeonieum.orderservice.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
