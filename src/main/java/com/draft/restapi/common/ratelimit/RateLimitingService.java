package com.draft.restapi.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
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
    private String timeToRefill;

    private final ObjectProvider<ProxyManager<byte[]>> proxyManagerProvider;

    private final ConcurrentHashMap<String, Bucket> localBuckets = new ConcurrentHashMap<>();

    public RateLimitingService(ObjectProvider<ProxyManager<byte[]>> proxyManagerProvider) {
        this.proxyManagerProvider = proxyManagerProvider;
    }

    public Bucket resolveBucket(String ipAddress) {
        Duration refillDuration = Duration.parse("PT" + timeToRefill.toUpperCase());
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, refillDuration)))
                .build();

        ProxyManager<byte[]> proxyManager = null;
        try {
            proxyManager = proxyManagerProvider.getIfAvailable();
        } catch (Exception e) {
            LOGGER.debug("Redis ProxyManager is not available, falling back to local buckets. Reason: {}", e.getMessage());
        }

        if (proxyManager != null) {
            return proxyManager.builder().build(ipAddress.getBytes(StandardCharsets.UTF_8), configuration);
        } else {
            return resolveLocalBucket(ipAddress);
        }
    }

    public Bucket resolveLocalBucket(String ipAddress) {
        Duration refillDuration = Duration.parse("PT" + timeToRefill.toUpperCase());
        return localBuckets.computeIfAbsent(ipAddress, key -> Bucket.builder().addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, refillDuration))).build());
    }

    public void clearLocalBuckets() {
        this.localBuckets.clear();
    }
}
