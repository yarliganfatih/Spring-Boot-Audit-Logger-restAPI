package com.draft.restapi.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    @Value("${spring.application.ratelimit.capacity:20}")
    private int capacity;

    @Value("${spring.application.ratelimit.time-to-refill:60s}")
    private String timeToRefill;

    private final ProxyManager<byte[]> proxyManager;

    private final ConcurrentHashMap<String, Bucket> localBuckets = new ConcurrentHashMap<>();

    public RateLimitingService(@Autowired(required = false) ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public Bucket resolveBucket(String ipAddress) {
        Duration refillDuration = Duration.parse("PT" + timeToRefill.toUpperCase());
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, refillDuration)))
                .build();

        if (proxyManager != null) {
            return proxyManager.builder().build(ipAddress.getBytes(StandardCharsets.UTF_8), configuration);
        } else {
            return localBuckets.computeIfAbsent(ipAddress, key -> Bucket.builder().addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, refillDuration))).build());
        }
    }

    public void clearLocalBuckets() {
        this.localBuckets.clear();
    }
}
