package com.yeonieum.orderservice.domain.regularorder.entity;

import com.yeonieum.orderservice.global.converter.RegularDeliveryStatusCodeConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "regular_delivery_status")
public class RegularDeliveryStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regular_delivery_status_id")
    private Long regularDeliveryStatusId;

    @Column(name = "status_name", nullable = false)
    private String statusName;
}

