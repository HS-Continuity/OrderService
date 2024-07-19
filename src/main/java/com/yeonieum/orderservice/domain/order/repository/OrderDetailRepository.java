package com.yeonieum.orderservice.domain.order.repository;

import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {
}
