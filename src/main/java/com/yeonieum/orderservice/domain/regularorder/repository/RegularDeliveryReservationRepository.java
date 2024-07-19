package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegularDeliveryReservationRepository extends JpaRepository<RegularDeliveryReservation, Long> {
}
