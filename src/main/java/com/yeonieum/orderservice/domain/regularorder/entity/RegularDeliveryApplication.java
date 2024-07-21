package com.yeonieum.orderservice.domain.regularorder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "regular_delivery_application")
public class RegularDeliveryApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regular_delivery_application_id")
    private Long regularDeliveryApplicationId;

    @Column(name = "main_product_id", nullable = false)
    public Long mainProductId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "member_payment_card_id", nullable = false)
    private String memberPaymentCardId;

    @Column(nullable = false)
    private int cycle;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String recipient;

    @Column(name = "recipient_phone_number", nullable = false)
    private String recipientPhoneNumber;

    @Column(nullable = false)
    private String address;

    @Column(name = "order_memo", length = 900)
    private String orderMemo;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
    //총배송회차
    @Column(name = "total_delivery_rounds", nullable = false)
    private int totalDeliveryRounds;
    //진행완료회차
    @Column(name = "completed_rounds", nullable = false)
    private int completedRounds;

    @Column(name = "ordered_product_count", nullable = false)
    private int orderedProductCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regular_delivery_status_id", nullable = false)
    private RegularDeliveryStatus regularDeliveryStatus;

    @OneToMany(mappedBy = "regularDeliveryApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RegularDeliveryReservation> regularDeliveryReservationList = new ArrayList<>();

    @OneToMany(mappedBy = "regularDeliveryApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RegularDeliveryApplicationDay> regularDeliveryApplicationDayList = new ArrayList<>();

    public void changeCompletedRounds(int completedRounds) {
        this.completedRounds = completedRounds;
    }

    public void changeDeliveryStatus(RegularDeliveryStatus status) {
        this.regularDeliveryStatus = status;
    }
}


