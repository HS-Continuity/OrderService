package com.yeonieum.orderservice.infrastructure.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderNotificationMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.RegularOrderNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderNotificationKafkaProducer {

    private final ObjectMapper objectMapper;
    private static final String ORDER_TOPIC = "order-notification-topic";
    private static final String REGULAR_TOPIC = "regular-notification-topic";

//    @Autowired
//    private KafkaTemplate<String, OrderNotificationMessage> orderKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(OrderNotificationMessage message) throws JsonProcessingException {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(ORDER_TOPIC, objectMapper.writeValueAsString(message));

        // 성공 및 실패 처리
        future.thenAccept(result -> {

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public void sendMessage(RegularOrderNotificationMessage message) throws JsonProcessingException {
        // CompletableFuture를 이용하여 메시지를 비동기적으로 보냅니다.
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(REGULAR_TOPIC, objectMapper.writeValueAsString(message));

        // 성공 및 실패 처리
        future.thenAccept(result -> {

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
