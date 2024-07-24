package com.yeonieum.orderservice.domain.combinedpackaging.entity;

import com.yeonieum.orderservice.domain.delivery.entity.Delivery;
import com.yeonieum.orderservice.domain.release.entity.Release;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "combined_packaging")
public class CombinedPackaging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "combined_packaging_id")
    private Long combinedPackagingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;
}
