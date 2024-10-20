package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.test.web.servlet.MockMvc;

import com.draft.restapi.audit.repository.ErrorLogRepository;
import com.draft.restapi.auth.repository.RoleRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@ExtendWith(SpringExtension.class)
@WebMvcTest(value = DraftController.class)
@MockBean(JpaMetamodelMappingContext.class) // Override EnableJpaAuditing on the main class
public class DraftControllerSliceTest {

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

    @MockBean
    private ErrorLogRepository errorLogRepository;

    @Test
    @WithMockUser
    public void testIndexEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello World!"));
    }

    @Test
    @WithMockUser
    public void testErrorEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"));
    }

    @Test
    @WithMockUser
    public void testParamEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/param?id=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("param is 5"));
    }

    @Test
    @WithMockUser
    public void testQueryEndpoint_caseMissingParam() throws Exception {
        mockMvc.perform(get("/api/draft/query?id="))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("param.id"))
                .andExpect(jsonPath("$.validationErrors[0].code").value("NotNull"));
    }

    @Test
    @WithMockUser
    public void testQueryEndpoint_caseInvalidType() throws Exception {
        mockMvc.perform(get("/api/draft/query?id=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("id"))
                .andExpect(jsonPath("$.validationErrors[0].code").value("typeMismatch"));
    }

    @Test
    @WithMockUser
    public void testPathEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/path/sample/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("sample is 10"));
    }

    @Test
    @WithMockUser
    public void testPathEndpoint_caseInvalidSlug() throws Exception {
        mockMvc.perform(get("/api/draft/path/sa/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(2));
    }

    @Test
    @WithMockUser
    public void testFormEndpoint() throws Exception {
        mockMvc.perform(post("/api/draft/form")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("key", "1")
                .param("field", "value")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("key is 1, field is value"));
    }

    @Test
    @WithMockUser
    public void testFormEndpoint_caseInvalidInput() throws Exception {
        mockMvc.perform(post("/api/draft/form")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("key", "0")
                .param("field", "v")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(2));
    }

    @Test
    @WithMockUser
    public void testFormEndpoint_caseInvalidType() throws Exception {
        mockMvc.perform(post("/api/draft/form")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("key", "abc")
                .param("field", "value")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("key"))
                .andExpect(jsonPath("$.validationErrors[0].code").value("typeMismatch"));
    }

    @Test
    @WithMockUser
    public void testUploadEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes());

        mockMvc.perform(multipart("/api/draft/upload")
                .file(file)
                .param("description", "A text file")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("File test.txt uploaded with description: A text file"));
    }

    @Test
    @WithMockUser
    public void testUploadEndpoint_caseMissingParam() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes());

        mockMvc.perform(multipart("/api/draft/upload")
                .file(file)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("description"))
                .andExpect(jsonPath("$.validationErrors[0].code").value("missingParam"));
    }

    @Test
    @WithMockUser
    public void testUploadEndpoint_caseMissingPart() throws Exception {
        mockMvc.perform(multipart("/api/draft/upload")
                .param("description", "A text file")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("file"))
                .andExpect(jsonPath("$.validationErrors[0].code").value("missingPart"));
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
                .andExpect(jsonPath("$.message").value("key is 1, field is value"));
    }

    @Test
    @WithMockUser
    public void testPostEndpoint_caseInvalidInput() throws Exception {
        String reqBody = "{\"key\":\"0\",\"field\":\"v\"}";

        mockMvc.perform(post("/api/draft/post")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(2));
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
                .andExpect(jsonPath("$.message").value("Hello admin"));
    }

    @Test
    @WithMockUser(username = "mod", authorities = "ROLE_mod")
    public void testModEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/mod"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello mod"));
    }

    @Test
    // Through role hierarchy
    @WithMockUser(username = "admin", authorities = "ROLE_admin")
    public void testModEndpointWithAdminUser() throws Exception {
        mockMvc.perform(get("/api/draft/mod"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello mod"));
    }

    @Test
    @WithMockUser(username = "chosen", authorities = "ROLE_chosen")
    public void testChosenEndpoint() throws Exception {
        mockMvc.perform(get("/api/draft/chosen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello chosen one"));
    }
}