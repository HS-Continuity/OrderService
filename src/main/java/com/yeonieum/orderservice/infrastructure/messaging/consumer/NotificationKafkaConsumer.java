package com.yeonieum.orderservice.infrastructure.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.domain.notification.service.OrderNotificationServiceForMember;
import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderNotificationMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.RegularOrderNotificationMessage;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationKafkaConsumer {
    private final OrderNotificationServiceForMember orderNotificationServiceForMember;
    private final ObjectMapper objectMapper;

    // Kafka Consumer를 이용하여 메시지를 받아 처리합니다.
    @KafkaListener(id = "order-notification-consumer", topics = "order-notification-topic", groupId = "order-notification-group", autoStartup = "true")
    public void listenOrderEventTopic(@Payload String message) {
        try {
            OrderNotificationMessage orderNotificationMessage = objectMapper.readValue(message, OrderNotificationMessage.class);
            orderNotificationServiceForMember.sendOrderMessage(orderNotificationMessage);
        } catch (JsonProcessingException e) {
            // 무시
        } catch (NurigoMessageNotReceivedException | NurigoEmptyResponseException |  NurigoUnknownException e) {
            // 문자인증 실패
        }
    }

    @KafkaListener(id = "regular-order-notification-consumer", topics = "regular-order-notification-topic", groupId = "order-notification-group", autoStartup = "true")
    public void listenRegularOrderEventTopic(@Payload String message) {
        try {
            RegularOrderNotificationMessage regularOrderNotificationMessage = objectMapper.readValue(message, RegularOrderNotificationMessage.class);
            orderNotificationServiceForMember.sendRegularOrderMessage(regularOrderNotificationMessage);
        } catch (JsonProcessingException e) {
            // 무시
        } catch (NurigoMessageNotReceivedException | NurigoEmptyResponseException |  NurigoUnknownException e) {
            // 문자인증 실패
        }
    }
}
