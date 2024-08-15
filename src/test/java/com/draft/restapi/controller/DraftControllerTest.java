package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.test.web.servlet.MockMvc;

import com.draft.restapi.auth.repository.RoleRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = DraftController.class)
@MockBean(JpaMetamodelMappingContext.class) // Override EnableJpaAuditing on the main class
public class DraftControllerTest {

    @TestConfiguration
    static class RoleHierarchyTestConfig {
        @Bean
        @Primary
        public RoleHierarchy roleHierarchy() {
            RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
            roleHierarchy.setHierarchy("ROLE_admin > ROLE_mod");
            return roleHierarchy;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private RoleRepository roleRepository;

    @Test
    @WithMockUser
    public void testIndexEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Hello World!\"}"));
    }

    @Test
    @WithMockUser
    public void testErrorEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/error"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"message\":\"Bad Request\"}"));
    }

    @Test
    @WithMockUser
    public void testParamEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/param?id=5"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"param is 5\"}"));
    }

    @Test
    @WithMockUser
    public void testPathEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/path/sample/10"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"sample is 10\"}"));
    }

    @Test
    @WithMockUser
    public void testPostEndpoint() throws Exception {
        String reqBody = "{\"key\":\"1\",\"field\":\"value\"}";

        mockMvc.perform(post("/api/draft/post")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"key is 1, field is value\"}"));
    }

    @Test
    // No user -> Unauthorized
    public void testPreAuthorizedEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    // Wrong role -> Forbidden
    @WithMockUser(username = "mod", authorities = "ROLE_mod")
    public void testAdminEndpointWithModUser() throws Exception {
        mockMvc.perform(get("/api/draft/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_admin")
    public void testAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/admin"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Hello admin\"}"));
    }

    @Test
    @WithMockUser(username = "mod", authorities = "ROLE_mod")
    public void testModEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/mod"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Hello mod\"}"));
    }

    @Test
    // Through role hierarchy
    @WithMockUser(username = "admin", authorities = "ROLE_admin")
    public void testModEndpointWithAdminUser() throws Exception {
        mockMvc.perform(get("/api/draft/mod"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Hello mod\"}"));
    }

    @Test
    @WithMockUser(username = "chosen", authorities = "ROLE_chosen")
    public void testChosenEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/chosen"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Hello chosen one\"}"));
    }
}