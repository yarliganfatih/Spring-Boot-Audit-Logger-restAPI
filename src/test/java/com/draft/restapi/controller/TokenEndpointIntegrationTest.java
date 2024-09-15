package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

public class TokenEndpointIntegrationTest extends BaseIntegrationTest {

    @Test
    // FilterChainProxy -> DispatcherServlet -> Controller (TokenEndpoint) => Response (200)
    public void testLogInAsUser() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic("mobile", "pin"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "password")
                .param("username", "user")
                .param("password", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists());
    }

    @Test
    // FilterChainProxy -> DispatcherServlet -> Controller (TokenEndpoint) -> InvalidGrantException => Response (400)
    public void testLogInAsUser_caseInvalidPassword() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic("mobile", "pin"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "password")
                .param("username", "user")
                .param("password", "invalid")) // because of invalid password
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }

    @Test
    // FilterChainProxy -> DispatcherServlet -> Controller (TokenEndpoint) -> InvalidGrantException => Response (400)
    public void testLogInAsUser_caseInvalidUsername() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic("mobile", "pin"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "password")
                .param("username", "invalid") // because of invalid username
                .param("password", "user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }

    @Test
    // FilterChainProxy -> DispatcherServlet -> Controller (TokenEndpoint) -> UnsupportedGrantTypeException => Response (400)
    public void testLogInAsUser_caseInvalidGrantType() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic("mobile", "pin"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "invalid") // because of invalid grant type
                .param("username", "user")
                .param("password", "user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
    }

    @Test
    // FilterChainProxy -> DispatcherServlet -> Controller (TokenEndpoint) -> InvalidRequestException => Response (400)
    public void testLogInAsUser_caseMissedGrantType() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic("mobile", "pin"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "") // because of missed grant type
                .param("username", "user")
                .param("password", "user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    // FilterChainProxy (BasicAuthenticationFilter) -> BadCredentialsException => Response (401)
    public void testLogInAsUser_caseInvalidClientId() throws Exception {
        // mockMvc does not handle response body in this case, but it works when running as spring boot application
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic("invalid", "pin")) // because of invalid client id
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "password")
                .param("username", "user")
                .param("password", "user"))
                .andExpect(status().isUnauthorized());
        //      .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    // FilterChainProxy (BasicAuthenticationFilter) -> BadCredentialsException => Response (401)
    public void testLogInAsUser_caseInvalidClientSecret() throws Exception {
        // mockMvc does not handle response body in this case, but it works when running as spring boot application
        mockMvc.perform(post("/oauth/token")
                .with(httpBasic("mobile", "invalid")) // because of invalid client secret
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "password")
                .param("username", "user")
                .param("password", "user"))
                .andExpect(status().isUnauthorized());
        //      .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}