package com.yeonieum.orderservice.domain.release.repository;

import com.yeonieum.orderservice.domain.release.dto.ReleaseResponse;
import com.yeonieum.orderservice.domain.release.dto.ReleaseSummaryResponse;
import com.yeonieum.orderservice.domain.release.entity.Release;
import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    @Query("SELECT r FROM Release r WHERE r.orderDetail.orderDetailId = :orderDetailId")
    Release findByOrderDetailId(String orderDetailId);

    @Query("SELECT r FROM Release r WHERE r.orderDetail.customerId = :customerId AND r.releaseStatus.statusName = :statusCode")
    Page<Release> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("statusCode") ReleaseStatusCode statusCode, Pageable pageable);

    @Query("SELECT r FROM Release r WHERE r.orderDetail.customerId = :customerId")
    Page<Release> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT new com.yeonieum.orderservice.domain.release.dto.ReleaseSummaryResponse(r.releaseStatus.statusName, COUNT(r)) " +
            "FROM Release r " +
            "WHERE r.orderDetail.customerId = :customerId " +
            "GROUP BY r.releaseStatus.statusName")
    List<ReleaseSummaryResponse> countByReleaseStatus(@Param("customerId") Long customerId);
}


