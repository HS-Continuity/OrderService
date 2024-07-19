package com.yeonieum.orderservice.domain.regularorder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "regular_delivery_reservation")
public class RegularDeliveryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regular_delivery_reservation_id")
    private long regularDeliveryReservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regular_delivery_status_id", nullable = false)
    private RegularDeliveryStatus regularDeliveryStatus;

    @Column(name = "delivery_rounds", nullable = false)
    private int deliveryRounds;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private int quantity;

    public void changeStatus(RegularDeliveryStatus status) {
        this.regularDeliveryStatus = status;
    }
}

