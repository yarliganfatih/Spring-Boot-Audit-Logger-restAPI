package com.draft.restapi.common.idempotency;

import org.springframework.cache.annotation.Cacheable;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Cacheable(
    value = "idempotency",
    keyGenerator = "idempotencyKeyGenerator",
    condition = "@idempotencyKeyGenerator.hasKey()",
    sync = true // TODO implement redisson to support distributed locking for idempotency
)
public @interface Idempotency {
}
