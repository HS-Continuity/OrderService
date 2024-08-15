package com.yeonieum.orderservice.domain.order.repository;

import com.yeonieum.orderservice.domain.order.dto.response.OrderSummaryResponse;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, String>, OrderDetailRepositoryCustom {
    @Query(value = "SELECT o FROM OrderDetail o WHERE o.memberId =" +
            ":memberId AND o.orderDateTime BETWEEN :startDate AND" +
            ":endDate ORDER BY o.orderDateTime DESC")
    Page<OrderDetail> findByMemberId(@Param("memberId")String memberId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    @Query("SELECT o FROM OrderDetail o WHERE o.customerId =" +
            ":customerId AND (:orderStatus IS NULL OR o.orderStatus =" +
            ":orderStatus) ORDER BY o.orderDateTime DESC")
    Page<OrderDetail> findByCustomerIdAndOrderStatus(@Param("customerId")Long customerId,
                                                     @Param("orderStatus") OrderStatus orderStatus,
                                                     Pageable pageable);

    @Query("SELECT COUNT(o) FROM OrderDetail o WHERE o.customerId = :customerId AND (:orderStatus IS NULL OR o.orderStatus = :orderStatus)")
    Long countByCustomerIdAndOrderStatus(@Param("customerId") Long customerId,
                                         @Param("orderStatus") OrderStatus orderStatus);

    @Query("SELECT new com.yeonieum.orderservice.domain.order.dto.response.OrderSummaryResponse(os.statusName, COUNT(od)) " +
            "FROM OrderDetail od " +
            "JOIN od.orderStatus os " +
            "WHERE od.customerId = :customerId " +
            "GROUP BY os.statusName")
    List<OrderSummaryResponse> countByCustomerIdGroupedByOrderStatus(@Param("customerId") Long customerId);

    @Query("SELECT o FROM OrderDetail o WHERE o.orderDetailId IN :orderDetailIdList AND o.customerId = :customerId")
    List<OrderDetail> findAllByIdAndCustomerId(@Param("orderDetailIdList") List<String> orderDetailIdList, @Param("customerId") Long customerId);

}

