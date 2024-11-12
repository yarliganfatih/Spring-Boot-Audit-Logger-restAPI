package com.draft.restapi.common.config;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CachingConfig extends CachingConfigurerSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingConfig.class);

    @Value("${spring.cache.redis.time-to-live:60m}")
    private Duration timeToLive;

    @Value("${spring.redis.max-timeout:500ms}")
    private Duration maxTimeout;

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

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple")
    public CacheManager simpleCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager idempotencyCacheManager(
            @Value("${spring.redis.host:localhost}") String host, 
            @Value("${spring.redis.port:6379}") int port,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        
        return new CacheManager() {
            private CacheManager delegate;
            
            private synchronized CacheManager getDelegate() {
                if (delegate == null) {
                    try {
                        Config redissonConfig = new Config();
                        redissonConfig.useSingleServer()
                                .setAddress("redis://" + host + ":" + port)
                                .setTimeout((int) maxTimeout.toMillis())
                                .setRetryAttempts(1)
                                .setRetryInterval(100);
                        RedissonClient redissonClient = Redisson.create(redissonConfig);
                        
                        Map<String, CacheConfig> configMap = new HashMap<>();
                        CacheConfig defaultCacheConfig = new CacheConfig(timeToLive.toMillis(), 0);
                        configMap.put("idempotency", defaultCacheConfig);
                        
                        RedissonSpringCacheManager redissonCacheManager = new RedissonSpringCacheManager(redissonClient, configMap);
                            
                        delegate = new CircuitBreakerCacheManager(redissonCacheManager, circuitBreakerRegistry);
                    } catch (Exception e) {
                        LOGGER.warn("Redisson connection failed: {}. Falling back to NoOpCache for idempotency.", e.getMessage());
                        return new NoOpCacheManager();
                    }
                }
                return delegate;
            }

            @Override
            @Nullable
            public Cache getCache(@NonNull String name) {
                CacheManager activeDelegate = getDelegate();
                if (activeDelegate instanceof NoOpCacheManager) {
                     // Reset delegate to try reconnecting on the next request
                     delegate = null;
                     return new NoOpCacheManager().getCache(name);
                }
                return activeDelegate.getCache(name);
            }

            @Override
            @NonNull
            public Collection<String> getCacheNames() {
                return getDelegate().getCacheNames();
            }
        };
    }

    @Bean(name = "idempotencyCacheManager")
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple")
    public CacheManager simpleIdempotencyCacheManager() {
        return new ConcurrentMapCacheManager("idempotency");
    }

    @Override
    public CacheErrorHandler errorHandler() {
        // Caching failover & Defense-in-Depth safety net: If an unwrapped cache implementation fails, ignore errors and continue without caching
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
