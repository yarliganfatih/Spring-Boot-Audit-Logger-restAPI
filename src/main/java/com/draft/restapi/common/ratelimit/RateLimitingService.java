package com.draft.restapi.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.ConsumptionProbe;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RateLimitingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingService.class);

    @Value("${spring.application.ratelimit.capacity:20}")
    private int capacity;

    @Value("${spring.application.ratelimit.time-to-refill:60s}")
    private Duration timeToRefill;

    private final ObjectProvider<ProxyManager<byte[]>> proxyManagerProvider;

    private final ConcurrentHashMap<String, Bucket> localBuckets = new ConcurrentHashMap<>();

    public RateLimitingService(ObjectProvider<ProxyManager<byte[]>> proxyManagerProvider) {
        this.proxyManagerProvider = proxyManagerProvider;
    }

    @CircuitBreaker(name = "redisRateLimiter", fallbackMethod = "consumeFallback")
    public ConsumptionProbe consumeToken(String ipAddress) {
        Bucket tokenBucket = resolveBucket(ipAddress);
        return tokenBucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe consumeFallback(String ipAddress, Exception e) {
        LOGGER.warn("Redis rate limiting failed for IP: {}. Falling back to local bucket.", ipAddress);
        Bucket localBucket = resolveLocalBucket(ipAddress);
        return localBucket.tryConsumeAndReturnRemaining(1);
    }

    private Bandwidth getLimit() {
        return Bandwidth.classic(capacity, Refill.intervally(capacity, timeToRefill));
    }

    public Bucket resolveBucket(String ipAddress) {
        ProxyManager<byte[]> proxyManager = proxyManagerProvider.getIfAvailable();
        if (proxyManager != null) {
            BucketConfiguration config = BucketConfiguration.builder().addLimit(getLimit()).build();
            return proxyManager.builder().build(ipAddress.getBytes(StandardCharsets.UTF_8), config);
        }
        return resolveLocalBucket(ipAddress);
    }

    public Bucket resolveLocalBucket(String ipAddress) {
        return localBuckets.computeIfAbsent(ipAddress, k -> Bucket.builder().addLimit(getLimit()).build());
    }

    public void clearLocalBuckets() {
        this.localBuckets.clear();
    }
}
