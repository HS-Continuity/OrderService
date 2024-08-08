package com.yeonieum.orderservice.domain.notification.service;

import com.yeonieum.orderservice.domain.notification.util.MessageBuilder;
import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderNotificationMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.RegularDeliveryNotificationMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderNotificationServiceForMember {
    DefaultMessageService defaultMessageService;

    @Value("${sms.apiKey}")
    private String apiKey;
    @Value("${sms.secretKey}")
    private String secretKey;
    @Value("${sms.apiUrl}")
    private String apiUrl;

    @PostConstruct
    public void init() {
        defaultMessageService = NurigoApp.INSTANCE.initialize(apiKey, secretKey, apiUrl);
    }


    public void sendOrderMessage(OrderNotificationMessage orderNotificationMessage) throws NurigoMessageNotReceivedException, NurigoEmptyResponseException, NurigoUnknownException {
        Message message = new Message();
        message.setFrom("01089387607");
        message.setTo(orderNotificationMessage.getPhoneNumber().replaceAll("-", ""));
    
        String text = MessageBuilder.createOrderMessage(orderNotificationMessage);
        message.setText("[연이음 주문 안내]\n\n" + text);

        defaultMessageService.send(message);
    }


    // 정기주문 메시지
    public void sendRegularOrderMessage(RegularDeliveryNotificationMessage regularDeliveryNotificationMessage) throws NurigoMessageNotReceivedException, NurigoEmptyResponseException, NurigoUnknownException {
        Message message = new Message();
        message.setFrom("01089387607");
        message.setTo(regularDeliveryNotificationMessage.getPhoneNumber());
        //message.setTo("01089387607");
        String text = "";
        switch (regularDeliveryNotificationMessage.getEventType()) {
            case "APPLY" -> {
                text = MessageBuilder.createRegularOrderMessage(regularDeliveryNotificationMessage);
            }
            case "POSTPONE" -> {
                text = MessageBuilder.postponeRegularOrderMessage(regularDeliveryNotificationMessage);
            }
            case "CANCEL" -> {
                text = MessageBuilder.cancelRegularOrderMessage(regularDeliveryNotificationMessage);
            }
            default -> throw new RuntimeException("알 수 없는 이벤트 타입입니다.");
        }
        message.setText(text);
        defaultMessageService.send(message);

    }
}
