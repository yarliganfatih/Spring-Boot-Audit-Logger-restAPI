package com.draft.restapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

public class RateLimitingIntegrationTest extends BaseIntegrationTest {

    private Map<String, Bucket> testBuckets;

    @BeforeEach
    public void setupRateLimit() {
        testBuckets = new HashMap<>();
        Mockito.when(rateLimitingService.resolveBucket(Mockito.anyString())).thenAnswer(invocation -> {
            String ipAddress = invocation.getArgument(0);
            return testBuckets.computeIfAbsent(ipAddress, key -> Bucket.builder()
                    .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofSeconds(3))))
                    .build());
        });
    }

    @Test
    @WithMockUser
    public void testRateLimiting() throws Exception {
        // first, send 2 requests and exceed the rate limit
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "1");
            });
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "0");
            });
        
        // then, try to send a third request
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isTooManyRequests())
            .andExpect(result -> {
                String retryAfter = result.getResponse().getHeader("X-Rate-Limit-Retry-After-Seconds");
                assertNotNull(retryAfter);
            });

        // wait for 3 seconds to allow the bucket to refill
        Thread.sleep(3000);

        // now, try again, should be allowed
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "1");
            });
    }

    @Test
    @WithMockUser
    public void testRateLimiting_caseNotFound() throws Exception {
        // first, send 2 different invalid requests and exceed the rate limit
        mockMvc.perform(get("/api/notFoundable1"))
            .andExpect(status().isNotFound())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "1");
            });
        mockMvc.perform(get("/api/notFoundable2"))
            .andExpect(status().isNotFound())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "0");
            });
        
        // then, try to send a third request
        mockMvc.perform(get("/api/notFoundable3"))
            .andExpect(status().isTooManyRequests())
            .andExpect(result -> {
                String retryAfter = result.getResponse().getHeader("X-Rate-Limit-Retry-After-Seconds");
                assertNotNull(retryAfter);
            });

        // try to send non-matching request, rate limiting will not handle
        mockMvc.perform(get("/notFoundable"))
            .andExpect(status().isNotFound());

        // wait for 3 seconds to allow the bucket to refill
        Thread.sleep(3000);

        // now, try again, should be allowed
        mockMvc.perform(get("/api/notFoundable4"))
            .andExpect(status().isNotFound())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "1");
            });
    }

    @Test
    @WithMockUser
    public void testRateLimiting_caseInsensitiveIp() throws Exception {
        // first, send a request from one IP
        mockMvc.perform(get("/api/draft")
            .with(request -> {
                request.setRemoteAddr("127.0.0.1");
                return request;
            }))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "1");
            });

        // then, send a request from another IP
        mockMvc.perform(get("/api/draft")
            .with(request -> {
                request.setRemoteAddr("127.0.0.2");
                return request;
            }))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals(remaining, "1"); // decrements independently for each IP
            });
    }
}
