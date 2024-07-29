package com.yeonieum.orderservice.infrastructure.feignclient.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RetrieveMemberSummary {
    private String memberName;
    private String memberPhoneNumber;
}