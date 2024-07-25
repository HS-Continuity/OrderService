package com.yeonieum.orderservice.infrastructure.feignclient.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RetrieveOrderInformationResponse {
    Long productId;
    String productName;
    String productImage;
    String storeName;
}