package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegularDeliveryApplicationRepository extends JpaRepository<RegularDeliveryApplication, Long> {
    @Query("SELECT r FROM RegularDeliveryApplication r WHERE r.memberId = :memberId ORDER BY r.createdDate ASC")
    Page<RegularDeliveryApplication> findByMemberIdOrderByCreatedAtAsc(@Param("memberId") String memberId, Pageable pageable);

    @Query("SELECT rda FROM RegularDeliveryApplication rda " +
            "LEFT JOIN FETCH rda.regularDeliveryReservationList rdr " +
            "WHERE rda.regularDeliveryApplicationId = :regularDeliveryApplicationId AND rdr.deliveryRounds > rda.completedRounds")
    RegularDeliveryApplication findByIdWithPendingReservations(@Param("regularDeliveryApplicationId") Long regularDeliveryApplicationId);

    @Query("SELECT rda FROM RegularDeliveryApplication rda " +
            "LEFT JOIN FETCH rda.regularDeliveryReservationList rdr " +
            "LEFT JOIN FETCH rda.regularDeliveryApplicationDayList rdad " +
            "WHERE rda.regularDeliveryApplicationId = :regularDeliveryApplicationId")
    RegularDeliveryApplication findWithReservationsAndApplicationDaysById(@Param("regularDeliveryApplicationId") Long regularDeliveryApplicationId);

}
