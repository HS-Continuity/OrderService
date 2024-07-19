package com.yeonieum.orderservice.domain.release.repository;

import com.yeonieum.orderservice.domain.release.entity.ReleaseStatus;
import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseStatusRepository extends JpaRepository<ReleaseStatus,Long> {
    ReleaseStatus findByStatusName(ReleaseStatusCode statusName);
}
