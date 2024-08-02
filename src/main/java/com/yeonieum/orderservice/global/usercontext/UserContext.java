package com.yeonieum.orderservice.global.usercontext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * HTTP 요청 사용자 정보 바인딩 객체
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class UserContext {
    public static final String TRANSACTION_ID = "transaction-id";
    public static final String AUTH_TOKEN = "auth-token";
    public static final String USER_ID = "user-id";
    public static final String UNIQUE_ID = "uniqueId";
    public static final String ROLE_TYPE = "role-type";
    public static final String SERVICE_ID = "service-id";

    @Builder.Default
    private String transactionId = new String();
    @Builder.Default
    private String authToken = new String();
    @Builder.Default
    private String userId = new String();
    @Builder.Default
    private String serviceId = new String();
    @Builder.Default
    private String uniqueId = new String();
    @Builder.Default
    private String roleType = new String();

}
