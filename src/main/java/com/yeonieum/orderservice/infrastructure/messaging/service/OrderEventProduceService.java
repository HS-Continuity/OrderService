package com.yeonieum.orderservice.infrastructure.messaging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderEventMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.RegularDeliveryEventMessage;
import com.yeonieum.orderservice.infrastructure.messaging.producer.OrderNotificationKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventProduceService {

    private final OrderNotificationKafkaProducer orderNotificationKafkaProducer;

    public void produceOrderEvent(String memberId, String orderDetailId,String topic ,String eventType) throws JsonProcessingException {
        orderNotificationKafkaProducer.sendMessage(OrderEventMessage.builder()
                .orderDetailId(orderDetailId)
                .memberId(memberId)
                .topic(topic)
                .eventType(eventType)
                .build());
    }

    public void produceRegularOrderEvent(String memberId, Long regularDeliveryId,  String topic, String eventType) throws JsonProcessingException {
        orderNotificationKafkaProducer.sendMessage(RegularDeliveryEventMessage.builder()
                .regularDeliveryId(regularDeliveryId)
                .memberId(memberId)
                .topic(topic)
                .eventType(eventType)
                .build());
    }
}
