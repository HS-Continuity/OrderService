package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RegularDeliveryApplicationRepository extends JpaRepository<RegularDeliveryApplication, Long> {
    @Query("SELECT r FROM RegularDeliveryApplication r " +
            "WHERE r.memberId = :memberId " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR r.createdDate BETWEEN :startDate AND :endDate) " +
            "ORDER BY r.createdDate ASC")
    Page<RegularDeliveryApplication> findByMemberIdAndCreatedDate(
            @Param("memberId") String memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT rda FROM RegularDeliveryApplication rda " +
            "LEFT JOIN FETCH rda.regularDeliveryReservationList rdr " +
            "WHERE rda.regularDeliveryApplicationId = :regularDeliveryApplicationId AND rdr.deliveryRounds > rda.completedRounds")
    RegularDeliveryApplication findByIdWithPendingReservations(@Param("regularDeliveryApplicationId") Long regularDeliveryApplicationId);
    @Query(value = "SELECT rda.* " +
            "FROM regular_delivery_application rda " +
            "LEFT JOIN regular_delivery_reservation rdr ON rda.regular_delivery_application_id = rdr.regular_delivery_application_id " +
            "LEFT JOIN regular_delivery_application_day rdad ON rda.regular_delivery_application_id = rdad.regular_delivery_application_id " +
            "WHERE rda.regular_delivery_application_id = :regularDeliveryApplicationId",
            nativeQuery = true)
    RegularDeliveryApplication findWithReservationsAndApplicationDaysById(@Param("regularDeliveryApplicationId") Long regularDeliveryApplicationId);


    //
}
