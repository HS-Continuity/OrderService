package com.yeonieum.orderservice.infrastructure.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.infrastructure.messaging.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final ObjectMapper objectMapper;
    public static final String ORDER_TOPIC = "order-notification-topic";
    public static final String REGULAR_TOPIC = "regular-notification-topic";
    public static final String SHIPPED_TOPIC = "shipped-order-topic";
    public static final String CANCEL_TOPIC = "cancel-order-topic";


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(OrderEventMessage message) throws JsonProcessingException {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(message.getTopic(), objectMapper.writeValueAsString(message));

        // 성공 및 실패 처리
        future.thenAccept(result -> {

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public void sendMessage(RegularDeliveryEventMessage message) throws JsonProcessingException {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(message.getTopic(), objectMapper.writeValueAsString(message));

        // 성공 및 실패 처리
        future.thenAccept(result -> {

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
    @Async
    public void sendApproveMessage(List<ShippedEventMessage> shippedEventMessages) throws JsonProcessingException {
        //objectMapper로 List<ShippedEventMessage> 역직렬화 변환


        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(SHIPPED_TOPIC, objectMapper.writeValueAsString(shippedEventMessages));

        future.thenAccept(result -> {

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
    @Async
    public void sendCancelMessage(List<ShippedEventMessage> shippedEventMessages) throws JsonProcessingException {
        //objectMapper로 List<ShippedEventMessage> 역직렬화 변환


        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(CANCEL_TOPIC, objectMapper.writeValueAsString(shippedEventMessages));

        future.thenAccept(result -> {

        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

}
