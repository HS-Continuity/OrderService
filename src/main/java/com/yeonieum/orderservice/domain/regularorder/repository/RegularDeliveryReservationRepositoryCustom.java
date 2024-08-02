package com.yeonieum.orderservice.domain.regularorder.repository;

import com.yeonieum.orderservice.domain.regularorder.dto.response.RegularOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RegularDeliveryReservationRepositoryCustom {
    List<RegularOrderResponse.OfRetrieveDailySummary> findRegularOrderCountsBetween(LocalDate startDate,
                                                                                   LocalDate endDate,
                                                                                   Long customerId);

    Page<RegularOrderResponse.OfRetrieveDailyDetail> findRegularOrderList(LocalDate date,
                                                                          Long customerId,
                                                                          Pageable pageable);
}
