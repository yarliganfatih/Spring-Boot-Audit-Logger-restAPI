package com.draft.restapi.common.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitingService {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    public Bucket resolveBucket(String ipAddress) {
        return cache.get(ipAddress, this::newBucket);
    }

    private Bucket newBucket(String ipAddress) {
        // Defines the rate limit: 20 requests per minute
        return Bucket.builder()
                .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1))))
                .build();
    }
}
