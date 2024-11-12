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
    cacheManager = "idempotencyCacheManager", // Utilizes Redisson Distributed Locks
    sync = true
)
public @interface Idempotency {
}
