package com.yeonieum.orderservice.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.domain.order.repository.OrderDetailRepository;
import com.yeonieum.orderservice.infrastructure.sse.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final SseEmitterRepository emitterRepository;
    private final RedisOperations<String, Long> orderEventRedisOperations;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final OrderDetailRepository orderDetailRepository;
    private final ObjectMapper objectMapper;


    public void sendEventMessage(Long customerId) {
        orderEventRedisOperations.convertAndSend("order:count" + customerId, customerId);
    }

    /**
     * 구독을 위해 호출하는 메서드.
     *
     * @param customerId
     * @return SseEmitter
     */
    public SseEmitter subscribe(Long customerId) throws IOException {
        SseEmitter emitter = createEmitter(customerId);
        emitter.send(SseEmitter.event()
                .id(String.valueOf(customerId))
                .name("order:count")
                .data("실시간 주문 알림 스트림 연결성공. [고객아이디=" + customerId + "]"));


        final MessageListener messageListener = (message, pattern) -> {
            Long destinationId = serialize(message);
            if(customerId == destinationId) {
                sendToClient(customerId, emitter);
            }
        };

        redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic.of("order:count" + customerId));
        return emitter;
    }

    private Long serialize(final Message message) {
        try {
            final Long customerId = this.objectMapper.readValue(message.getBody(), Long.class);
            return customerId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize message.", e);
        }
    }


    /**
     * 고객에게 주문상태별 주문건수를 전송
     *
     * @param customerId   - 데이터를 받을 사용자의 아이디.
     * @param - 전송할 데이터.
     */
    private void sendToClient(Long customerId, SseEmitter emitter) {
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(customerId))
                        .name("order:count")
                        .data(orderDetailRepository.countByCustomerIdGroupedByOrderStatus(customerId)));
            } catch (IOException exception) {
                emitterRepository.deleteById(customerId, emitter);
                emitter.completeWithError(exception);
            }
        }
    }

    /**
     * 사용자 아이디를 기반으로 이벤트 Emitter를 생성
     *
     * @param customerId
     * @return SseEmitter
     */
    private SseEmitter createEmitter(Long customerId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(customerId, emitter);
        return emitter;
    }
}
