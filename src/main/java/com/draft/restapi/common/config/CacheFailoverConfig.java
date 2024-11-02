package com.draft.restapi.common.config;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class CacheFailoverConfig extends CachingConfigurerSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFailoverConfig.class);

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                LOGGER.warn("Cache GET failed for cache '{}', ignoring and executing method... Reason: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, @Nullable Object value) {
                LOGGER.warn("Cache PUT failed for cache '{}'. Reason: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                LOGGER.warn("Cache EVICT failed for cache '{}'. Reason: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
                LOGGER.warn("Cache CLEAR failed for cache '{}'. Reason: {}", cache.getName(), exception.getMessage());
            }
        };
    }
}
