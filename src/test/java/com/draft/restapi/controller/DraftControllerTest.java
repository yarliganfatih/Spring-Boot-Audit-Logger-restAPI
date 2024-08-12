package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.draft.restapi.RestapiApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RestapiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DraftControllerTest {

    @LocalServerPort
    private int port;

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    @Test
    public void testIndexEndpoint() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/draft"),
                HttpMethod.GET, entity, String.class);
        String expected = "{\"message\":\"Hello World!\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    public void testErrorEndpoint() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/draft/error"),
                HttpMethod.GET, entity, String.class);
        String expected = "{\"message\":\"Bad Request\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    public void testParamEndpoint() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/draft/param?id=5"),
                HttpMethod.GET, entity, String.class);
        String expected = "{\"message\":\"param is 5\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    public void testPathEndpoint() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/draft/path/sample/10"),
                HttpMethod.GET, entity, String.class);
        String expected = "{\"message\":\"sample is 10\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    public void testPostEndpoint() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String reqBody = "{\"key\":\"1\",\"field\":\"value\"}";
        HttpEntity<String> entity = new HttpEntity<>(reqBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/draft/post"),
                HttpMethod.POST, entity, String.class);

        String expected = "{\"message\":\"key is 1, field is value\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }
    
    @Test
    public void testPreAuthorizedEndpoint() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/api/draft/admin"),
                HttpMethod.GET, entity, String.class);
        String expected = "{\"error\":\"unauthorized\",\"error_description\":\"Full authentication is required to access this resource\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }
}