package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;

import java.time.LocalDate;
import java.util.List;

public interface RegularDeliveryReservationRepositoryCustom {
    List<RegularOrderResponse.OfRetrieveDailyCount> findRegularOrderCountsBetween(LocalDate startDate,
                                                                                  LocalDate endDate,
                                                                                  Long customerId);
}
