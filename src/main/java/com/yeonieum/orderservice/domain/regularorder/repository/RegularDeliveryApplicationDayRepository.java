package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplicationDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegularDeliveryApplicationDayRepository extends JpaRepository<RegularDeliveryApplicationDay, Long> {
}
