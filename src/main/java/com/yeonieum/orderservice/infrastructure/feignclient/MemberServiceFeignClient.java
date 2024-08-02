package com.yeonieum.orderservice.infrastructure.feignclient;

import com.yeonieum.orderservice.domain.order.dto.response.OrderResponse;
import com.yeonieum.orderservice.global.config.FeignConfig;
import com.yeonieum.orderservice.global.responses.ApiResponse;
import com.yeonieum.orderservice.infrastructure.feignclient.dto.response.RetrieveMemberSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "memberservice", configuration = FeignConfig.class)
public interface MemberServiceFeignClient {

    @GetMapping("/memberservice/api/member/summaries")
    ResponseEntity<ApiResponse<List<OrderResponse.MemberInfo>>> getOrderMemberInfos(@RequestParam List<String> memberIds);

    @GetMapping("/memberservice/api/member/filter")
    ResponseEntity<ApiResponse<List<String>>> getOrderMemberFilter(@RequestParam(required = false) String memberName, @RequestParam(required = false) String memberPhoneNumber);

    @GetMapping("/memberservice/api/member/order")
    ResponseEntity<ApiResponse<OrderResponse.MemberInfo>> getOrderMemberInfo(@RequestParam String memberId);

    @PutMapping("/memberservice/api/member-coupon/use-status")
    ResponseEntity<ApiResponse<Boolean>> useMemberCouponStatus(@RequestParam Long memberCouponId);

    @GetMapping("/memberservice/api/member/summary")
    ResponseEntity<ApiResponse<RetrieveMemberSummary>> getMemberSummary(@RequestParam String memberId);
    @GetMapping("/memberservice/api/member/list/order")
    public ResponseEntity<ApiResponse<Map<String, OrderResponse.MemberInfo>>> getOrderMemberInfo(@RequestParam List<String> memberIds);

    @GetMapping("/memberservice/api/member/filter-map")
    ResponseEntity<ApiResponse<Map<String, OrderResponse.MemberInfo>>> getFilterMemberMap(@RequestParam(required = false) String memberName,
                                                                                          @RequestParam(required = false) String memberPhoneNumber);
    @GetMapping("/memberservice/api/member/statistics")
    ResponseEntity<ApiResponse<OrderResponse.MemberStatistics>> getOrderMemberStatistics(@RequestParam String memberId);
    }
