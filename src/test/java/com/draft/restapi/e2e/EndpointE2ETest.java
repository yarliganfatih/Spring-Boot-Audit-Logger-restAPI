package com.draft.restapi.e2e;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EndpointE2ETest extends BaseE2ETest {

    @Test
    // FilterChainProxy -> DispatcherServlet -> Controller => Response (200)
    public void testRunning() throws Exception {
        ResponseEntity<String> response = restTemplate
                .exchange(
                        createURLWithPort("/"),
                        HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    // FilterChainProxy -> DispatcherServlet -> NoHandlerFoundException => Response (404)
    public void testEndpoint_caseNotFound() throws Exception {
        ResponseEntity<String> response = restTemplate
                .exchange(
                        createURLWithPort("/notFoundable/"),
                        HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    // FilterChainProxy (ExceptionTranslationFilter) -> AccessDeniedException => Response (401)
    public void testEndpoint_caseUnAuthorized() throws Exception {
        ResponseEntity<String> response = restTemplate
                .exchange(
                        createURLWithPort("/api/test/"),
                        HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    // FilterChainProxy (OncePerRequestFilter) => StrictHttpFirewall => RequestRejectedException => Response (500)
    public void testEndpoint_caseInvalidUrl() throws Exception {
        ResponseEntity<String> response = restTemplate
                .exchange(
                        createURLWithPort("/api/../"),
                        HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}