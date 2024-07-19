package com.yeonieum.orderservice.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "payment_information")
public class PaymentInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_information_id")
    private Long paymentInformationId;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "delivery_fee")
    private int deliveryFee;

    @Column(name = "discount_amount", nullable = false)
    private int discountAmount;

    @Column(name = "origin_product_price", nullable = false)
    private int originProductPrice;

    @Column(name = "payment_amount", nullable = false)
    private int paymentAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;
}

