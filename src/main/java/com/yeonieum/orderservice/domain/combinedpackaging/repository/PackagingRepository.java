package com.yeonieum.orderservice.domain.combinedpackaging.repository;

import com.yeonieum.orderservice.domain.combinedpackaging.entity.Packaging;
import com.yeonieum.orderservice.domain.delivery.dto.DeliverySummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {

        @Query(value = "SELECT d.delivery_id, d.shipment_number, d.delivery_status_id, r.start_delivery_date, od.order_detail_id, COUNT(od.order_detail_id), GROUP_CONCAT(od.product_order_list ORDER BY od.order_detail_id SEPARATOR ','), od.member_id " +
                "FROM packaging p " +
                "JOIN delivery d ON p.delivery_id = d.delivery_id " +
                "JOIN release_table r ON p.release_id = r.release_id " +
                "JOIN order_detail od ON r.order_detail_id = od.order_detail_id " +
                "WHERE od.customer_id = :customerId " +
                "GROUP BY d.delivery_id", nativeQuery = true)
        List<Object[]> findAllDeliveryInfo(@Param("customerId") Long customerId);

        @Query("SELECT new com.yeonieum.orderservice.domain.delivery.dto.DeliverySummaryResponse(d.deliveryStatus.statusName, COUNT(p)) " +
                "FROM Packaging p " +
                "JOIN p.delivery d " +
                "JOIN p.orderDetail od " +
                "WHERE od.customerId = :customerId " +
                "GROUP BY d.deliveryStatus.statusName")
        List<DeliverySummaryResponse> countByDeliveryStatus(@Param("customerId") Long customerId);
}




