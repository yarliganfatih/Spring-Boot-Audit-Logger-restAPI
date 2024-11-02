package com.draft.restapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.draft.restapi.RestapiApplication;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;

@SuppressWarnings("unchecked")
@SpringBootTest(classes = RestapiApplication.class, properties = { 
        "spring.application.ratelimit.capacity=2",
        "spring.application.ratelimit.time-to-refill=3s" })
public class RateLimitingIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private ProxyManager<byte[]> proxyManager;

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

    @Test
    @WithMockUser
    public void testRateLimiting_redisFailover() throws Exception {
        // simulate Redis failure during consumption, fallback to local bucket
        RemoteBucketBuilder<byte[]> mockBuilder = Mockito.mock(RemoteBucketBuilder.class);
        BucketProxy mockBucket = Mockito.mock(BucketProxy.class);
        Mockito.when(proxyManager.builder()).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.build(Mockito.any(byte[].class), Mockito.any(BucketConfiguration.class))).thenReturn(mockBucket);
        Mockito.when(mockBucket.tryConsumeAndReturnRemaining(1))
               .thenThrow(new RuntimeException("Redis connection timed out"));

        // first request should fail on Redis, consume 1, leave 1
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals("1", remaining);
            });

        // second request should fail on Redis, consume 1, leave 0
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals("0", remaining);
            });

        // third request should exceed local bucket limit
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isTooManyRequests())
            .andExpect(result -> {
                String retryAfter = result.getResponse().getHeader("X-Rate-Limit-Retry-After-Seconds");
                assertNotNull(retryAfter);
            });

        // wait for 3 seconds to allow the local bucket to refill
        Thread.sleep(3000);

        // now, try again, local bucket should be refilled and allowed
        mockMvc.perform(get("/api/draft"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String remaining = result.getResponse().getHeader("X-Rate-Limit-Remaining");
                assertEquals("1", remaining);
            });
    }
}
