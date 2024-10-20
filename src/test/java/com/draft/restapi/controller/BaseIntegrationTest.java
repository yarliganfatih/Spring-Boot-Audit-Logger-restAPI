package com.draft.restapi.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.repository.EntityLogRepository;
import com.draft.restapi.RestapiApplication;
import com.fasterxml.jackson.databind.ObjectMapper;

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
