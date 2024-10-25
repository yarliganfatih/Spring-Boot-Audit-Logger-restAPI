package com.draft.restapi.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.repository.EntityLogRepository;
import com.draft.restapi.common.ratelimit.RateLimitingService;
import com.draft.restapi.RestapiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@SuppressWarnings("null")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RestapiApplication.class)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
 
    @Autowired
    protected EntityLogRepository entityLogRepository;

    @Autowired
    protected CacheManager cacheManager;

    @MockBean
    protected RateLimitingService rateLimitingService;

    @BeforeEach
    public void setupRateLimit() {
        Bucket unlimitedBucket = Bucket.builder()
                .addLimit(Bandwidth.classic(10000, Refill.intervally(10000, Duration.ofMinutes(1))))
                .build();
        Mockito.when(rateLimitingService.resolveBucket(Mockito.anyString())).thenReturn(unlimitedBucket);
    }

    @AfterEach
    public void clearCache() {
        cacheManager.getCacheNames().forEach(cacheName -> 
            cacheManager.getCache(cacheName).clear() // to avoid cache bleeding between tests
        );
    }

    protected void assertAuditLogs(String entityName, Long entityId, String expectedOperation) {
        List<EntityLog> logs = entityLogRepository.getEntitiyLogs(entityName, entityId);
        assertNotNull(logs);
        assertTrue(logs.size() > 0, "Should have audit logs");
        boolean hasOperation = logs.stream().anyMatch(log -> expectedOperation.equals(log.getOperation()));
        assertTrue(hasOperation, "Should have " + expectedOperation + " operation log");
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
