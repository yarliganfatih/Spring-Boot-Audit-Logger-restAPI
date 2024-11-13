package com.draft.restapi.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.LogEvent;

import com.draft.restapi.audit.repository.AuditLogElasticRepository;
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
 
    @MockBean
    protected AuditLogElasticRepository auditLogElasticRepository;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected RateLimitingService rateLimitingService;

    protected static final List<String> capturedAuditLogs = new ArrayList<>();

    @BeforeAll
    public static void setupLogger() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        AbstractAppender appender = new AbstractAppender("TestAppender", null, null, true, null) {
            @Override
            public void append(LogEvent event) {
                capturedAuditLogs.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        config.getLoggerConfig("AUDIT_LOGGER").removeAppender("AsyncAuditAppender");
        config.getLoggerConfig("AUDIT_LOGGER").addAppender(appender, null, null);
        context.updateLoggers();
    }

    @AfterEach
    public void clearState() {
        capturedAuditLogs.clear();
        
        // to avoid cache bleeding between tests
        if (cacheManager.getCacheNames() != null) {
            cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        }
        if (rateLimitingService != null) {
            rateLimitingService.clearLocalBuckets();
        }
    }

    protected void assertAuditLogs(String entityName, Long entityId, String expectedOperation) {
        // Since AsyncAppender might delay log delivery slightly, we wait briefly if needed
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean hasOperation = capturedAuditLogs.stream().anyMatch(log -> 
            log.contains("\"entityName\":\"" + entityName + "\"") && 
            log.contains("\"operation\":\"" + expectedOperation + "\"") &&
            (entityId == null || log.contains("\"entityId\":" + entityId))
        );
        assertTrue(hasOperation, "Should have " + expectedOperation + " operation log for " + entityName);
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
