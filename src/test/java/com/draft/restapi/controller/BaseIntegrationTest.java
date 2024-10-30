package com.draft.restapi.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.repository.EntityLogRepository;
import com.draft.restapi.common.ratelimit.RateLimitingService;
import com.draft.restapi.RestapiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("null")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RestapiApplication.class)
@ActiveProfiles("test")
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

    @Autowired
    protected RateLimitingService rateLimitingService;

    @AfterEach
    public void clearCache() {
        // to avoid cache bleeding between tests
        if (cacheManager.getCacheNames() != null) {
            cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        }
        if (rateLimitingService != null) {
            rateLimitingService.clearLocalBuckets();
        }
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
