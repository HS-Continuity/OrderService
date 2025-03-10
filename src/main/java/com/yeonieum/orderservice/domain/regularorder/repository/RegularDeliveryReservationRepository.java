package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RegularDeliveryReservationRepository extends JpaRepository<RegularDeliveryReservation, Long>, RegularDeliveryReservationRepositoryCustom {
    @Query("SELECT r FROM RegularDeliveryReservation r " +
            "JOIN r.regularDeliveryApplication a " +
            "WHERE a.regularDeliveryApplicationId = :applicationId " +
            "AND r.deliveryRounds = :deliveryRounds")
    List<RegularDeliveryReservation> findByDeliveryApplicationAndProductId(@Param("applicationId") Long applicationId,
                                                                           @Param("deliveryRounds") int deliveryRounds);

}
