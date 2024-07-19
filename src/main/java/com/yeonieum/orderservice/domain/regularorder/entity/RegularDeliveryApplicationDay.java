package com.yeonieum.orderservice.domain.regularorder.entity;

import com.yeonieum.orderservice.global.converter.DayOfWeekConverter;
import com.yeonieum.orderservice.global.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "regular_delivery_application_day")
public class RegularDeliveryApplicationDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regular_delivery_application_day_id")
    private Long regularDeliveryApplicationDayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regular_delivery_application_id", nullable = false)
    private RegularDeliveryApplication regularDeliveryApplication;

    @Convert(converter = DayOfWeekConverter.class)
    @Column(name = "day_code", nullable = false)
    private DayOfWeek dayCode;
}


