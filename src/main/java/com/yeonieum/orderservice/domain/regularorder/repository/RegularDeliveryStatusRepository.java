package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegularDeliveryStatusRepository extends JpaRepository<RegularDeliveryStatus, Long> {
    RegularDeliveryStatus findByStatusName(String statusName);
}
