package com.yeonieum.orderservice.infrastructure.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeonieum.orderservice.domain.notification.service.OrderNotificationServiceForMember;
import com.yeonieum.orderservice.domain.order.entity.OrderDetail;
import com.yeonieum.orderservice.domain.order.repository.OrderDetailRepository;
import com.yeonieum.orderservice.domain.regularorder.entity.RegularDeliveryApplication;
import com.yeonieum.orderservice.domain.regularorder.repository.RegularDeliveryApplicationRepository;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.infrastructure.feignclient.MemberServiceFeignClient;
import com.yeonieum.orderservice.infrastructure.feignclient.ProductServiceFeignClient;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.RetrieveMemberSummary;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.RetrieveOrderInformationResponse;
import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderEventMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderNotificationMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.RegularOrderNotificationMessage;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationKafkaConsumer {
    private final OrderNotificationServiceForMember orderNotificationServiceForMember;
    private final OrderDetailRepository orderDetailRepository;
    private final RegularDeliveryApplicationRepository regularDeliveryApplicationRepository;
    private final MemberServiceFeignClient memberServiceFeignClient;
    private final ProductServiceFeignClient productServiceFeignClient;
    private final ObjectMapper objectMapper;

    // Kafka Consumer를 이용하여 메시지를 받아 처리합니다.
    @KafkaListener(id = "order-notification-consumer", topics = "order-notification-topic", groupId = "order-notification-group", autoStartup = "true")
    public void listenOrderEventTopic(@Payload String message) {
        try {
            OrderEventMessage orderEventMessage = objectMapper.readValue(message, OrderEventMessage.class);
            OrderNotificationMessage orderNotificationMessage = orderNotificationMessageBuilder(
                    orderEventMessage.getMemberId(),
                    orderEventMessage.getOrderDetailId(),
                    orderEventMessage.getEventType()
            );
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
            OrderEventMessage orderEventMessage = objectMapper.readValue(message, OrderEventMessage.class);
            RegularOrderNotificationMessage regularOrderNotificationMessage = regularOrderNotificationMessageBuilder(
                    orderEventMessage.getMemberId(),
                    Long.parseLong(orderEventMessage.getOrderDetailId()),
                    orderEventMessage.getEventType()
            );

            orderNotificationServiceForMember.sendRegularOrderMessage(regularOrderNotificationMessage);
        } catch (JsonProcessingException e) {
            // 무시
        } catch (NurigoMessageNotReceivedException | NurigoEmptyResponseException |  NurigoUnknownException e) {
            // 문자인증 실패
        }
    }


    public OrderNotificationMessage orderNotificationMessageBuilder(String memberId, String orderDetailId, String eventType) {
        Optional<OrderDetail> orderDetailOptional = orderDetailRepository.findById(orderDetailId);

        if(orderDetailOptional.isEmpty()) {
            return null;
        }

        ResponseEntity<ApiResponse<RetrieveMemberSummary>> memberResponse = null;
        ResponseEntity<ApiResponse<RetrieveOrderInformationResponse>> productResponse = null;
        try {
            memberResponse = memberServiceFeignClient.getMemberSummary(memberId);
            productResponse = productServiceFeignClient.retrieveOrderProductInformation(orderDetailOptional.get().getMainProductId());
        } catch (FeignException e) {
            return null;
        }


        RetrieveMemberSummary memberSummary = memberResponse.getBody().getResult();
        RetrieveOrderInformationResponse productInformation = productResponse.getBody().getResult();
        OrderDetail orderDetail = orderDetailOptional.get();
        if(memberResponse.getStatusCode().is2xxSuccessful()) {
            return OrderNotificationMessage.convertedBy(
                    orderDetail
                    , productInformation
                    , memberSummary.getMemberPhoneNumber()
                    , memberSummary.getMemberName()
                    , eventType);
        }


        return null;
    }


    public RegularOrderNotificationMessage regularOrderNotificationMessageBuilder(String memberId, Long regularDeliveryId, String eventType) {
        ResponseEntity<ApiResponse<RetrieveMemberSummary>> memberResponse = null;
        ResponseEntity<ApiResponse<RetrieveOrderInformationResponse>> productResponse = null;
        Optional<RegularDeliveryApplication> regularDeliveryApplicationOptional =
                regularDeliveryApplicationRepository.findById(regularDeliveryId);

        RegularDeliveryApplication regularDeliveryApplication = regularDeliveryApplicationOptional.get();

        try {
            memberResponse = memberServiceFeignClient.getMemberSummary(memberId);
            productResponse = productServiceFeignClient.retrieveOrderProductInformation(regularDeliveryApplication.getMainProductId());
        } catch (FeignException e) {
            return null;
        }


        if(memberResponse.getStatusCode().is2xxSuccessful()) {
            RetrieveMemberSummary memberSummary = memberResponse.getBody().getResult();
            RetrieveOrderInformationResponse productInformation = productResponse.getBody().getResult();
            return RegularOrderNotificationMessage.builder()
                    .productName(productInformation.getProductName())
                    .productCount(regularDeliveryApplication.getOrderedProductCount())
                    .completedOrderCount(regularDeliveryApplication.getCompletedRounds())
                    .memberName(memberSummary.getMemberName())
                    .address(regularDeliveryApplication.getAddress())
                    .nextDeliveryDate(regularDeliveryApplication.getNextDeliveryDate())
                    .eventType(eventType)
                    .phoneNumber(memberSummary.getMemberPhoneNumber())
                    .build();
        }

        return null;
    }
}
