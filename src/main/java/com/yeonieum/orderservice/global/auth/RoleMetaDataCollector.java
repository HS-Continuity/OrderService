package com.yeonieum.orderservice.global.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class RoleMetaDataCollector {

    private final Map<String, RoleMetaData> roleMetadata = new HashMap<>();
    private final ApplicationContext applicationContext;

    @Autowired
    public RoleMetaDataCollector(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void collectRoleMetadata() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RestController.class);

        for (Object bean : beans.values()) {
            Class<?> beanClass = bean.getClass();
            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                Role secured = method.getAnnotation(Role.class);
                if (secured != null) {
                    String methodName = beanClass.getName() + "." + method.getName();
                    roleMetadata.put(secured.url(), RoleMetaData.builder()
                            .roles(Arrays.asList(secured.role()))
                            .Methods(secured.method())
                            .build());
                }
            }
        }
    }

    public Map<String, RoleMetaData> getRoleMetadata() {
        return roleMetadata;
    }
}