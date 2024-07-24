package com.yeonieum.orderservice.domain.combinedpackaging.repository;

import com.yeonieum.orderservice.domain.combinedpackaging.entity.Packaging;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackagingRepository extends JpaRepository<Packaging, Long> {
}
