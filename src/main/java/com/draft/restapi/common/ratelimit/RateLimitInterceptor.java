package com.draft.restapi.common.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingService rateLimitingService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String clientIp = getClientIP(request);
        Bucket tokenBucket = rateLimitingService.resolveBucket(clientIp);
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

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

    private String getClientIP(HttpServletRequest request) {
        String xffHeader = request.getHeader("X-Forwarded-For");
        if (xffHeader == null) {
            return request.getRemoteAddr();
        }
        return xffHeader.split(",")[0];
    }
}
