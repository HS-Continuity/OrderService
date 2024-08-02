package com.yeonieum.orderservice.domain.statistics.repository;

import com.yeonieum.orderservice.domain.statistics.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsRepository extends JpaRepository<Statistics, Long>, StatisticsRepositoryCustom {
}
