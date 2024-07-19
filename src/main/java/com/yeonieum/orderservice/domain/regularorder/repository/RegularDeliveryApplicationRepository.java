package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegularDeliveryApplicationRepository extends JpaRepository<RegularDeliveryApplication, Long> {
}
