package com.yeonieum.orderservice.domain.delivery.entity;

import com.yeonieum.orderservice.global.converter.DeliveryStatusCodeConverter;
import com.yeonieum.orderservice.global.enums.DeliveryStatusCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "delivery_status")
public class DeliveryStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_status_id")
    private Long deliveryStatusId;

    @Convert(converter = DeliveryStatusCodeConverter.class)
    @Column(name = "status_name", nullable = false)
    private DeliveryStatusCode statusName;

    @OneToMany(mappedBy = "deliveryStatus", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Delivery> deliveryList = new ArrayList<>();
}

