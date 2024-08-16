package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import com.draft.restapi.audit.entity.EntityLog;
import com.draft.restapi.audit.repository.EntityLogRepository;
import com.draft.restapi.RestapiApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RestapiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseControllerTest {

    @Autowired
    protected EntityLogRepository entityLogRepository;

    @LocalServerPort
    protected int port;

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    protected ResponseEntity<String> logIn(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("mobile", "pin")
                .exchange(
                        createURLWithPort("/oauth/token"),
                        HttpMethod.POST, entity, String.class);
        return response;
    }
    
    protected String logInAsUserAndGetBearerToken() throws Exception {
        ResponseEntity<String> response = logIn("user", "user");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    @Test
    public void testLogInAsUser() throws Exception {
        ResponseEntity<String> response = logIn("user", "user");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    protected void assertAuditLogs(String entityName, Long entityId, String expectedOperation) {
        List<EntityLog> logs = entityLogRepository.getEntitiyLogs(entityName, entityId);
        assertNotNull(logs);
        assertTrue(logs.size() > 0, "Should have audit logs");
        boolean hasOperation = logs.stream().anyMatch(log -> expectedOperation.equals(log.getOperation()));
        assertTrue(hasOperation, "Should have " + expectedOperation + " operation log");
    }
}
