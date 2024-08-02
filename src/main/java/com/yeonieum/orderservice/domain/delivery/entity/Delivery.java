package com.yeonieum.orderservice.domain.delivery.entity;

import com.yeonieum.orderservice.domain.combinedpackaging.entity.Packaging;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_status_id", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "shipment_number", nullable = false)
    private String shipmentNumber;

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Packaging> packagingList = new ArrayList<>();
}

