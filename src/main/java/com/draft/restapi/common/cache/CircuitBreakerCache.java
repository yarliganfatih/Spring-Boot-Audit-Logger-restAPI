package com.draft.restapi.common.cache;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cache.Cache;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class CircuitBreakerCache implements Cache {

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
        return executeWithFallback(() -> delegate.get(key), null);
    }

    @Override
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        return executeWithFallback(() -> delegate.get(key, type), null);
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        try {
            return circuitBreaker.executeSupplier(() -> delegate.get(key, valueLoader));
        } catch (CallNotPermittedException e) {
            try {
                // If cache is skipped, we must invoke the valueLoader to get the actual value (e.g. from DB)
                return valueLoader.call();
            } catch (Exception ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }
        }
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        executeRunnable(() -> delegate.put(key, value));
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, @Nullable Object value) {
        return executeWithFallback(() -> delegate.putIfAbsent(key, value), null);
    }

    @Override
    public void evict(@NonNull Object key) {
        executeRunnable(() -> delegate.evict(key));
    }

    @Override
    public void clear() {
        executeRunnable(() -> delegate.clear());
    }

    private <T> T executeWithFallback(Supplier<T> supplier, T fallback) {
        try {
            return circuitBreaker.executeSupplier(supplier);
        } catch (CallNotPermittedException e) {
            return fallback;
        }
    }

    private void executeRunnable(Runnable runnable) {
        try {
            circuitBreaker.executeRunnable(runnable);
        } catch (CallNotPermittedException e) {
            // Ignore
        }
    }
}
