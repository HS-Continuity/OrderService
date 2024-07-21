package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegularDeliveryReservationRepository extends JpaRepository<RegularDeliveryReservation, Long>, RegularDeliveryReservationRepositoryCustom {
    @Query("SELECT r FROM RegularDeliveryReservation r " +
            "JOIN r.regularDeliveryApplication a " +
            "WHERE a.regularDeliveryApplicationId = :applicationId " +
            "AND r.productId = :productId " +
            "AND r.deliveryRounds = a.completedRounds + 1")
    RegularDeliveryReservation findByDeliveryApplicationAndProductId(@Param("applicationId") Long applicationId,
                                                                     @Param("productId") Long productId);

}
