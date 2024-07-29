package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;

import java.time.LocalDate;
import java.util.List;

public interface RegularDeliveryReservationRepositoryCustom {
    List<RegularOrderResponse.OfRetrieveDailySummary> findRegularOrderCountsBetween(LocalDate startDate,
                                                                                   LocalDate endDate,
                                                                                   Long customerId);

    List<RegularOrderResponse.OfRetrieveDailyDetail> findRegularOrderList(LocalDate date,
                                                                          Long customerId,
                                                                          int startPage,
                                                                          int pageSize);
}
