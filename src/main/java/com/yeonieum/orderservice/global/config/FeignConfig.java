package com.yeonieum.orderservice.global.config;

import com.yeonieum.orderservice.web.interceptor.UserContextInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }
}
