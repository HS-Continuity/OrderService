package com.yeonieum.orderservice.web.filter;

import com.yeonieum.orderservice.global.usercontext.UserContext;
import com.yeonieum.orderservice.global.usercontext.UserContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class UserContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        System.out.println(httpServletRequest.getHeader(UserContext.AUTH_TOKEN));
        System.out.println(httpServletRequest.getHeader("uniqueId" + UserContext.UNIQUE_ID));

        //헤더 목록 순회하기
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + httpServletRequest.getHeader(headerName));
        }


        UserContextHolder.getContext().builder()
                        .authToken(httpServletRequest.getHeader(UserContext.AUTH_TOKEN))
                        .transactionId(httpServletRequest.getHeader(UserContext.TRANSACTION_ID))
                        .userId(httpServletRequest.getHeader(UserContext.USER_ID))
                        .serviceId(httpServletRequest.getHeader(UserContext.SERVICE_ID))
                        .uniqueId(httpServletRequest.getHeader(UserContext.UNIQUE_ID))
                        .roleType(httpServletRequest.getHeader(UserContext.ROLE_TYPE))
                        .build();
        chain.doFilter(request, response);
    }
}
