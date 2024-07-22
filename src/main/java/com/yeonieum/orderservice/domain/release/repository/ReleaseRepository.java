package com.yeonieum.orderservice.domain.release.repository;

import com.yeonieum.orderservice.domain.release.entity.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    @Query("SELECT r FROM Release r WHERE r.orderDetail.orderDetailId = :orderDetailId")
    Release findByOrderDetailId(String orderDetailId);
}
