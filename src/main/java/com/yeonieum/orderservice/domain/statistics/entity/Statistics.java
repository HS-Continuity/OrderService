package com.yeonieum.orderservice.domain.statistics.entity;

import com.yeonieum.orderservice.global.enums.Gender;
import com.yeonieum.orderservice.global.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "statistics")
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistics_id")
    private Long statisticsId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "age_range", nullable = false)
    private int ageRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Column(name = "price", nullable = false)
    private int price;
}
