package com.draft.restapi.common.idempotency;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component("idempotencyKeyGenerator")
public class IdempotencyKeyGenerator implements KeyGenerator {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    @Override
    @NonNull
    public Object generate(@NonNull Object target, @NonNull Method method, @NonNull Object... params) {
        String headerValue = getIdempotencyKeyFromRequest();
        if (headerValue != null) {
            return headerValue;
        }
        return SimpleKey.EMPTY;
    }

    public boolean hasKey() {
        return getIdempotencyKeyFromRequest() != null;
    }

    private String getIdempotencyKeyFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            HttpServletRequest request = attributes.getRequest();
            String key = request.getHeader(IDEMPOTENCY_KEY_HEADER);
            if (key != null && !key.trim().isEmpty()) {
                return key.trim();
            }
        }
        return null;
    }
}
