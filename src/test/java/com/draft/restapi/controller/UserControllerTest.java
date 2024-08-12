package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

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

import com.draft.restapi.RestapiApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RestapiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<String> logIn(String username, String password) {
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

    public String logInAsUserAndGetBearerToken() throws Exception {
        ResponseEntity<String> response = logIn("user", "user");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

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

    public JsonNode createMockUser(Integer mock_id) throws Exception {
        String authToken = logInAsUserAndGetBearerToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);

        String username = "user" + mock_id;
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
        int uniqueNum = new Random().nextInt(1000);
        JsonNode mockUser = createMockUser(uniqueNum);
        String username = "user" + uniqueNum;
        assertEquals(username + "@example.com", mockUser.get("email").asText());
        assertEquals(username, mockUser.get("username").asText());
        // TODO control audit logs
    }

    @Test
    public void testGetUser() throws Exception {
        int uniqueNum = new Random().nextInt(1000);
        JsonNode mockUser = createMockUser(uniqueNum);
        String username = "user" + uniqueNum;

        Integer user_id = mockUser.get("id").asInt();
        String authToken = logInAsUserAndGetBearerToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users/" + user_id),
                HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode gotUser = mapper.readTree(response.getBody());
        assertEquals(user_id, gotUser.get("id").asInt());
        assertEquals(username + "@example.com", gotUser.get("email").asText());
        assertEquals(username, gotUser.get("username").asText());
    }

    @Test
    public void testUpdateUser() throws Exception {
        int uniqueNum = new Random().nextInt(1000);
        JsonNode mockUser = createMockUser(uniqueNum);
        String username = "updateduser" + uniqueNum;

        Integer user_id = mockUser.get("id").asInt();
        String authToken = logInAsUserAndGetBearerToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);

        String userJson = "{\"email\": \"" + username + "@example.com\", \"username\": \"" + username + "\", \"password\": \"updateduser\"}";
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users/" + user_id),
                HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode gotUser = mapper.readTree(response.getBody());
        assertEquals(user_id, gotUser.get("id").asInt());
        assertEquals(username + "@example.com", gotUser.get("email").asText());
        assertEquals(username, gotUser.get("username").asText());
        // TODO control audit logs
    }

    @Test
    public void testDeleteUser() throws Exception {
        int uniqueNum = new Random().nextInt(1000);
        JsonNode mockUser = createMockUser(uniqueNum);
        Integer user_id = mockUser.get("id").asInt();
        System.err.println("user_id : " + user_id);
        String authToken = logInAsUserAndGetBearerToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + authToken);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/users/" + user_id),
                HttpMethod.DELETE, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // TODO control audit logs
    }
}