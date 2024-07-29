package com.yeonieum.orderservice.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Content-Type", "application/json", "Authorization", "Bearer")
                .maxAge(3600);
    }
}