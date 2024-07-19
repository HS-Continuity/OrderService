package com.yeonieum.orderservice.domain.release.entity;

import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "release")
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "release_id")
    private Long releaseId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_status_id", nullable = false)
    private ReleaseStatus releaseStatus;

    @Column(length = 900)
    private String memo;

    @Column(name = "hold_reason", length = 900)
    private String holdReason;
}

