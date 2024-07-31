package com.yeonieum.orderservice.domain.combinedpackaging.repository;

import com.yeonieum.orderservice.domain.combinedpackaging.entity.Packaging;
import com.yeonieum.orderservice.domain.delivery.dto.DeliverySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {

@Query(value = "SELECT d.delivery_id, d.shipment_number, d.delivery_status_id, r.start_delivery_date, od.order_detail_id, COUNT(od.order_detail_id), GROUP_CONCAT(od.product_order_list ORDER BY od.order_detail_id SEPARATOR ','), od.member_id " +
        "FROM packaging p " +
        "JOIN delivery d ON p.delivery_id = d.delivery_id " +
        "JOIN release_table r ON p.release_id = r.release_id " +
        "JOIN order_detail od ON r.order_detail_id = od.order_detail_id " +
        "WHERE od.customer_id = :customerId " +
        "AND (:startDate IS NULL OR r.start_delivery_date >= :startDate) " +
        "AND (:endDate IS NULL OR r.start_delivery_date <= :endDate) " +
        "AND (:shipmentNumber IS NULL OR d.shipment_number LIKE %:shipmentNumber%) " +
        "AND (:deliveryStatusCode IS NULL OR d.delivery_status_id LIKE %:deliveryStatusCode%) " +
        "AND (:memberId IS NULL OR od.member_id LIKE %:memberId%) " +
        "GROUP BY d.delivery_id",
        countQuery = "SELECT COUNT(d.delivery_id) " +
                "FROM packaging p " +
                "JOIN delivery d ON p.delivery_id = d.delivery_id " +
                "JOIN release_table r ON p.release_id = r.release_id " +
                "JOIN order_detail od ON r.order_detail_id = od.order_detail_id " +
                "WHERE od.customer_id = :customerId " +
                "AND (:startDate IS NULL OR r.start_delivery_date >= :startDate) " +
                "AND (:endDate IS NULL OR r.start_delivery_date <= :endDate) " +
                "AND (:shipmentNumber IS NULL OR d.shipment_number LIKE %:shipmentNumber%) " +
                "AND (:deliveryStatusCode IS NULL OR d.delivery_status_id LIKE %:deliveryStatusCode%) " +
                "AND (:memberId IS NULL OR od.member_id LIKE %:memberId%)",
        nativeQuery = true)
Page<Object[]> findAllDeliveryInfo(@Param("customerId") Long customerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("shipmentNumber") String shipmentNumber, @Param("deliveryStatusCode") String deliveryStatusCode, @Param("memberId") String memberId, Pageable pageable);
}




