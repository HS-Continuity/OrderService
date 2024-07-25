package com.yeonieum.orderservice.domain.delivery.repository;

import com.yeonieum.orderservice.domain.delivery.dto.DeliveryResponse;
import com.yeonieum.orderservice.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

}
