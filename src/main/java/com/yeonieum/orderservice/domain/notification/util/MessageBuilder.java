package com.yeonieum.orderservice.domain.notification.util;

import com.yeonieum.orderservice.infrastructure.messaging.dto.OrderNotificationMessage;
import com.yeonieum.orderservice.infrastructure.messaging.dto.RegularOrderNotificationMessage;

public class MessageBuilder {

    private static String createHeader(String eventType) {
        switch (eventType) {
            case "PAYMENT_COMPLETED" -> {
                return "[연이음] 주문완료 안내\n\n";
            }
            case "CANCELED" -> {
                return "[연이음] 주문취소 안내\n\n";
            }
            case "SHIPPED" -> {
                return "[연이음] 배송시작 안내\n\n";
            }
            default -> throw new RuntimeException("알 수 없는 이벤트 타입입니다.");
        }
    }

    private static String createContentMessage(String eventType) {
        switch (eventType) {
            case "PAYMENT_COMPLETED" -> {
                return "주문완료 되었습니다. \n";
            }
            case "CANCELED" -> {
                return "주문이 취소되었습니다. \n";
            }
            case "SHIPPED" -> {
                return "주문하신 상품이 배송 시작되었습니다. \n";
            }
            default -> throw new RuntimeException("알 수 없는 이벤트 타입입니다.");
        }

    }

    public static String createOrderMessage(OrderNotificationMessage orderNotificationMessage) {
        // 일반 주문 완료
        return  createHeader(orderNotificationMessage.getEventType())+
                "안녕하세요. " + orderNotificationMessage.getMemberName() + "님!\n" +
                createContentMessage(orderNotificationMessage.getEventType()) +
                "\n" +
                "▶ 상품명 : " + orderNotificationMessage.getProductName() + "외 " + orderNotificationMessage.getProductName() + "건\n" +
                "▶ 주문번호 : " + orderNotificationMessage.getOrderNumber();
    }


    public static String createRegularOrderMessage(RegularOrderNotificationMessage regularOrderNotificationMessage) {

        // 정기배송 신청 완료 시 메시지
        return "[연이음] 정기배송 신청완료 안내\n" +
                "\n" +
                "안녕하세요," + regularOrderNotificationMessage.getMemberName() + "님!\n" +
                "고객님의 정기배송 서비스 신청이 완료되었습니다. \n" +
                "신청하신 상품을 지정한 날짜에 자동으로 배송해 드립니다.\n" +
                "\n" +
                "행사기간에 따라 상품 가격이 변동되기 때문에 신청 시와 결제 시 가격이 다를 수 있습니다.\n" +
                "결제시점 가격을 기준으로 정기배송 할인율이 적용되니 이용에 참고 부탁드립니다.\n" +
                "\n" +
                "▶정기배송지 : " + regularOrderNotificationMessage.getAddress() + "\n" +
                "▶정기배송상품 :" +  regularOrderNotificationMessage.getProductName() + " 외 "+ regularOrderNotificationMessage.getProductCount() + "건\n" +
                "▶다음 정기배송일 : " + regularOrderNotificationMessage.getNextDeliveryDate().toString() + "일";
    }

    public static String cancelRegularOrderMessage(RegularOrderNotificationMessage regularOrderNotificationMessage) {
        // 정기배송 신청 취소 시 메시지
        return "[연이음] 정기배송 신청취소 안내\n" +
                "\n" +
                "안녕하세요," + regularOrderNotificationMessage.getMemberName() + "님!\n" +
                "고객님의 정기배송 서비스 신청이 취소되었습니다. \n" +
                "정기배송 서비스가 취소되어 정기배송 상품이 배송되지 않습니다.\n" +
                "\n" +
                "▶정기배송상품 :" +  regularOrderNotificationMessage.getProductName() + " 외 "+ regularOrderNotificationMessage.getProductCount() + "건\n";
    }

    public static String postponeRegularOrderMessage(RegularOrderNotificationMessage regularOrderNotificationMessage) {
        // 정기배송 일시 정지 시 메시지
        return "[연이음] 정기배송 회차 건너뛰기 안내\n" +
                "\n" +
                "안녕하세요," + regularOrderNotificationMessage.getMemberName() + "님!\n" +
                "고객님의 정기배송 서비스가 일시 정지되었습니다. \n" +
                "정기배송 서비스 " + regularOrderNotificationMessage.getCompletedOrderCount() + "회차 정기배송 상품이 배송되지 않습니다.\n" +
                "\n" +
                "▶정기배송상품 :" +  regularOrderNotificationMessage.getProductName() + " 외 "+ regularOrderNotificationMessage.getProductCount() + "건\n";

    }
}
