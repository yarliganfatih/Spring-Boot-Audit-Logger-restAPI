package com.draft.restapi.common.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitingService rateLimitingService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String clientIp = request.getRemoteAddr(); // XFF handling by SERVER_FORWARD_HEADERS_STRATEGY

        ConsumptionProbe probe;
        try {
            Bucket tokenBucket = rateLimitingService.resolveBucket(clientIp);
            probe = tokenBucket.tryConsumeAndReturnRemaining(1);
        } catch (Exception e) {
            LOGGER.warn("Redis rate limiting failed for IP: {}. Falling back to local bucket.", clientIp);
            Bucket localBucket = rateLimitingService.resolveLocalBucket(clientIp);
            probe = localBucket.tryConsumeAndReturnRemaining(1);
        }

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "You have exhausted your API Request Quota");
            return false;
        }
    }
}
