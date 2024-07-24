package com.yeonieum.orderservice.domain.delivery.entity;

import com.yeonieum.orderservice.domain.combinedpackaging.entity.CombinedPackaging;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_status_id", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "shipment_number")
    private String shipmentNumber;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CombinedPackaging> combinedPackagingList = new ArrayList<>();
}

