package com.yeonieum.orderservice.domain.order.entity;

import com.yeonieum.orderservice.global.converter.OrderStatusCodeConverter;
import com.yeonieum.orderservice.global.enums.OrderStatusCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_status_id")
    private Long orderStatusId;

    @Convert(converter = OrderStatusCodeConverter.class)
    @Column(name = "status_name", nullable = false)
    private OrderStatusCode statusName;
}

