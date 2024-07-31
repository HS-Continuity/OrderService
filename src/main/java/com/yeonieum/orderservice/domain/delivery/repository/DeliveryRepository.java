package com.yeonieum.orderservice.domain.delivery.repository;

import com.yeonieum.orderservice.domain.delivery.dto.DeliverySummaryResponse;
import com.yeonieum.orderservice.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT new com.yeonieum.orderservice.domain.delivery.dto.DeliverySummaryResponse(d.deliveryStatus.statusName, COUNT(d)) " +
            "FROM Delivery d " +
            "WHERE EXISTS ( " +
            "   SELECT 1 FROM Packaging p " +
            "   JOIN p.orderDetail od " +
            "   WHERE p.delivery = d AND od.customerId = :customerId " +
            ") " +
            "GROUP BY d.deliveryStatus.statusName")
    List<DeliverySummaryResponse> countByDeliveryStatusForCustomer(@Param("customerId") Long customerId);

}
