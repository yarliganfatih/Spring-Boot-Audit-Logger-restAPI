package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserControllerTest extends BaseControllerTest {

    @Test
    public void testGetAllUsers() throws Exception {
        String authToken = logInAsUserAndGetBearerToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users"),
                HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    public JsonNode createMockUser(Integer mockId, String authToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);

        String username = "user" + mockId;
        String userJson = "{\"email\": \"" + username + "@example.com\", \"username\": \"" + username + "\", \"password\": \"" + username + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users"),
                HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode createdUser = mapper.readTree(response.getBody());
        return createdUser;
    }

    @Test
    public void testCreateUser() throws Exception {
        // given
        int uniqueNum = new Random().nextInt(1000);

        // when
        String authToken = logInAsUserAndGetBearerToken();
        JsonNode mockUser = createMockUser(uniqueNum, authToken);

        // then
        String username = "user" + uniqueNum;
        Integer userId = mockUser.get("id").asInt();
        assertEquals(username + "@example.com", mockUser.get("email").asText());
        assertEquals(username, mockUser.get("username").asText());
        assertAuditLogs("users", userId.longValue(), "CREATE");
    }

    @Test
    public void testGetUser() throws Exception {
        // given
        int uniqueNum = new Random().nextInt(1000);
        String authToken = logInAsUserAndGetBearerToken();
        JsonNode mockUser = createMockUser(uniqueNum, authToken);
        String username = "user" + uniqueNum;
        Integer userId = mockUser.get("id").asInt();

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users/" + userId),
                HttpMethod.GET, entity, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode gotUser = mapper.readTree(response.getBody());
        assertEquals(userId, gotUser.get("id").asInt());
        assertEquals(username + "@example.com", gotUser.get("email").asText());
        assertEquals(username, gotUser.get("username").asText());
    }

    @Test
    public void testUpdateUser() throws Exception {
        // given
        int uniqueNum = new Random().nextInt(1000);
        String authToken = logInAsUserAndGetBearerToken();
        JsonNode mockUser = createMockUser(uniqueNum, authToken);
        String username = "updateduser" + uniqueNum;
        Integer userId = mockUser.get("id").asInt();

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);
        String userJson = "{\"email\": \"" + username + "@example.com\", \"username\": \"" + username + "\", \"password\": \"updateduser\"}";
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users/" + userId),
                HttpMethod.PUT, entity, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode gotUser = mapper.readTree(response.getBody());
        assertEquals(userId, gotUser.get("id").asInt());
        assertEquals(username + "@example.com", gotUser.get("email").asText());
        assertEquals(username, gotUser.get("username").asText());
        assertAuditLogs("users", userId.longValue(), "UPDATE");
    }

    @Test
    public void testDeleteUser() throws Exception {
        // given
        int uniqueNum = new Random().nextInt(1000);
        String authToken = logInAsUserAndGetBearerToken();
        JsonNode mockUser = createMockUser(uniqueNum, authToken);
        Integer userId = mockUser.get("id").asInt();

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users/" + userId),
                HttpMethod.DELETE, entity, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertAuditLogs("users", userId.longValue(), "DELETE");
    }
}