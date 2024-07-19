package com.yeonieum.orderservice.domain.order.entity;

import com.yeonieum.orderservice.global.converter.ProductOrderListConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @Column(name = "order_detail_id")
    private String orderDetailId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status_id", nullable = false)
    private OrderStatus orderStatus;

    @Column(name ="customer_id" , nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String recipient;

    @Column(name = "recipient_phone_number", nullable = false)
    private String recipientPhoneNumber;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "order_memo", nullable = false)
    private String orderMemo;

    @Column(name = "order_date_time", nullable = false)
    private LocalDateTime orderDateTime;

    @Column(name = "product_order_list", nullable = false, length = 40000)
    @Convert(converter = ProductOrderListConverter.class)
    private ProductOrderListEntity orderList;

    public void changeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

