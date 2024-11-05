package com.draft.restapi.common.config;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.draft.restapi.common.cache.CircuitBreakerCacheManager;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.cache.CacheManager;
import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CachingConfig extends CachingConfigurerSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingConfig.class);

    @Value("${spring.cache.redis.time-to-live:60m}")
    private Duration timeToLive;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager cacheManager(@NonNull RedisConnectionFactory redisConnectionFactory, CircuitBreakerRegistry circuitBreakerRegistry) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive);
        
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
                
        return new CircuitBreakerCacheManager(redisCacheManager, circuitBreakerRegistry);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        // Caching failover: If Redis is down, continue without caching
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
