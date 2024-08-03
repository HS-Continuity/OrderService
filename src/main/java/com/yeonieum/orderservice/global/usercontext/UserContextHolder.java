package com.yeonieum.orderservice.global.usercontext;

import org.springframework.util.Assert;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static final UserContext getContext() {
        UserContext context = userContext.get();

        if (context == null) {
            context = createEmptyContext();
            setContext(context);
        }

        return context;
    }


    public static void setContext(UserContext context) {
        Assert.notNull(context, "null인 UserContext 객체를 설정할 수 없습니다.");
        userContext.set(context);
    }

    public static final UserContext createEmptyContext() {
        return new UserContext();
    }
}
