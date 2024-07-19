package com.yeonieum.orderservice.domain.order.service;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.domain.order.entity.PaymentInformation;
import com.yeonieum.orderservice.domain.order.repository.PaymentInformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final PaymentInformationRepository paymentInformationRepository;

    /**
     * 주문 상세 ID로 결제 정보 조회
     * @param orderDetailId
     * @return
     */
    @Transactional(readOnly = true)
    public OrderResponse.OfRetrievePayment retrievePaymentInformation (String orderDetailId){
        PaymentInformation paymentInformation = paymentInformationRepository.findByOrderDetail_OrderDetailId(orderDetailId);
        return OrderResponse.OfRetrievePayment.convertedBy(paymentInformation);
    }
}
