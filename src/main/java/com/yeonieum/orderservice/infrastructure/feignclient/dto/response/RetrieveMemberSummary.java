package com.yeonieum.orderservice.infrastructure.feignclient.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RetrieveMemberSummary {
    private String memberName;
    private String memberPhoneNumber;
}