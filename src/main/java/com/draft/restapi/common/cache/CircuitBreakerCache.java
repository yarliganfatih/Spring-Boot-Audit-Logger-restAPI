package com.draft.restapi.common.cache;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class CircuitBreakerCache implements Cache {
    private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerCache.class);

    private final Cache delegate;

    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerCache(Cache delegate, CircuitBreaker circuitBreaker) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
    }

    @NonNull
    @Override
    public String getName() {
        return delegate.getName();
    }

    @NonNull
    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        return executeWithFallback(() -> delegate.get(key), null, "GET", key);
    }

    @Override
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        return executeWithFallback(() -> delegate.get(key, type), null, "GET", key);
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        try {
            return circuitBreaker.executeSupplier(() -> delegate.get(key, valueLoader));
        } catch (Cache.ValueRetrievalException e) {
            // Business logic threw an exception during a cache miss.
            // This is NOT a cache failure. Rethrow it directly.
            throw e;
        } catch (Exception e) {
            if (!(e instanceof CallNotPermittedException)) {
                LOGGER.warn("Redis cache read failed for key '{}' on cache '{}'. Falling back to database/method execution without cache. Reason: {}", key, delegate.getName(), e.getMessage());
            }
            try {
                // If cache is down (Redis timeout/connection error or open circuit breaker), invoke valueLoader directly to keep high-availability without 500 error
                return valueLoader.call();
            } catch (Exception ex) {
                throw new Cache.ValueRetrievalException(key, valueLoader, ex);
            }
        }
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        executeRunnable(() -> delegate.put(key, value), "PUT", key);
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, @Nullable Object value) {
        return executeWithFallback(() -> delegate.putIfAbsent(key, value), null, "PUT_IF_ABSENT", key);
    }

    @Override
    public void evict(@NonNull Object key) {
        executeRunnable(() -> delegate.evict(key), "EVICT", key);
    }

    @Override
    public void clear() {
        executeRunnable(() -> delegate.clear(), "CLEAR", "ALL");
    }

    private <T> T executeWithFallback(Supplier<T> supplier, T fallback, String operation, Object key) {
        try {
            return circuitBreaker.executeSupplier(supplier);
        } catch (Exception e) {
            if (!(e instanceof CallNotPermittedException)) {
                LOGGER.warn("Redis cache {} failed for key '{}' on cache '{}'. Falling back without cache. Reason: {}", operation, key, delegate.getName(), e.getMessage());
            }
            return fallback;
        }
    }

    private void executeRunnable(Runnable runnable, String operation, Object key) {
        try {
            circuitBreaker.executeRunnable(runnable);
        } catch (Exception e) {
            if (!(e instanceof CallNotPermittedException)) {
                LOGGER.warn("Redis cache {} failed for key '{}' on cache '{}'. Ignoring error to maintain application availability. Reason: {}", operation, key, delegate.getName(), e.getMessage());
            }
            // Ignore exception so API request completes successfully without 500 server error
        }
    }
}
