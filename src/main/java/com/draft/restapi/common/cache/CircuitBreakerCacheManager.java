package com.draft.restapi.common.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;

import java.util.Collection;

public class CircuitBreakerCacheManager implements CacheManager {

    private final CacheManager delegate;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerCacheManager(CacheManager delegate, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.delegate = delegate;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Cache getCache(@NonNull String name) {
        Cache cache = delegate.getCache(name);
        if (cache != null) {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("redisCache");
            return new CircuitBreakerCache(cache, circuitBreaker);
        }
        return null;
    }

    @NonNull
    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
