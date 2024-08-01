package com.yeonieum.orderservice.infrastructure.messaging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderEventMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.RegularDeliveryEventMessage;
import com.yeonieum.orderservice.infrastructure.messaging.producer.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventProduceService {

    private final OrderEventProducer orderEventProducer;

    @Async
    public void produceOrderEvent(String memberId, Long customerId, String orderDetailId,String topic ,String eventType) throws JsonProcessingException {
        orderEventProducer.sendMessage(OrderEventMessage.builder()
                .orderDetailId(orderDetailId)
                .memberId(memberId)
                .customerId(customerId)
                .topic(topic)
                .eventType(eventType)
                .build());
    }

    @Async
    public void produceRegularOrderEvent(String memberId, Long customerId, Long regularDeliveryId,  String topic, String eventType) throws JsonProcessingException {
        orderEventProducer.sendMessage(RegularDeliveryEventMessage.builder()
                .regularDeliveryId(regularDeliveryId)
                .memberId(memberId)
                .customerId(customerId)
                .topic(topic)
                .eventType(eventType)
                .build());
    }
}
