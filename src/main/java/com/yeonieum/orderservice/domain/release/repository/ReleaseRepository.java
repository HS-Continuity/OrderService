package com.yeonieum.orderservice.domain.release.repository;

import com.yeonieum.orderservice.domain.release.dto.ReleaseSummaryResponse;
import com.yeonieum.orderservice.domain.release.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long>, ReleaseRepositoryCustom {

    @Query("SELECT r FROM Release r JOIN FETCH r.orderDetail od WHERE od.orderDetailId = :orderDetailId AND od.customerId = :customerId")
    Release findByOrderDetailId(@Param("orderDetailId") String orderDetailId, @Param("customerId") Long customerId);

    @Query("SELECT new com.yeonieum.orderservice.domain.release.dto.ReleaseSummaryResponse(r.releaseStatus.statusName, COUNT(r)) " +
            "FROM Release r " +
            "WHERE r.orderDetail.customerId = :customerId " +
            "GROUP BY r.releaseStatus.statusName")
    List<ReleaseSummaryResponse> countByReleaseStatus(@Param("customerId") Long customerId);
}


