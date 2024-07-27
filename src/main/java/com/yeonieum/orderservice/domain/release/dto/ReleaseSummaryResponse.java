package com.yeonieum.orderservice.domain.release.dto;

import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReleaseSummaryResponse {
        ReleaseStatusCode statusName;
        Long count;
}
