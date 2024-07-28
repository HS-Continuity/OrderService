package com.yeonieum.orderservice.web.filter;

import com.yeonieum.orderservice.global.usercontext.UserContext;
import com.yeonieum.orderservice.global.usercontext.UserContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

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
