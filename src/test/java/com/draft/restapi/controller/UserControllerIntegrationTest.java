package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@Sql(scripts = {"classpath:db/sql/insert-user-data.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:db/sql/insert-user-data-rollback.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser() throws Exception {
        String username = "createdUser";
        String email = username + "@example.com";
        String password = username + "123";
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Integer userId = JsonPath.read(responseContent, "$.data.id");
        assertAuditLogs("users", userId.longValue(), "CREATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_caseDuplicateError() throws Exception {
        String username = "mockUser"; // because of sending existing value
        String email = username + "2@example.com"; // unique
        String password = username + "123";
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("duplicate"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("username"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_caseValidationError() throws Exception {
        String username = "created-user"; // because of using hyphen which is invalid pattern
        String email = ""; // because of sending empty value
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\"}"; // because of missing password field

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(3));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetUser() throws Exception {
        Integer userId = 2;

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value("mockUser@example.com"))
                .andExpect(jsonPath("$.data.username").value("mockUser"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetUser_caseNotFound() throws Exception {
        Integer userId = 999;

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id : '" + userId + "'"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser() throws Exception {
        String username = "updatedUser";
        String email = username + "@example.com";
        String password = username + "123";
        Integer userId = 2;
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));
        assertAuditLogs("users", userId.longValue(), "UPDATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseNotFound() throws Exception {
        String username = "updatedUser";
        String email = username + "@example.com";
        String password = username + "123";
        Integer userId = 999;
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id : '" + userId + "'"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseNullId() throws Exception {
        String username = "updatedUser";
        String email = username + "@example.com";
        String password = username + "123";
        Integer userId = null;
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testDeleteUser() throws Exception {
        Integer userId = 2;

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk());
        assertAuditLogs("users", userId.longValue(), "DELETE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testDeleteUser_caseNotFound() throws Exception {
        Integer userId = 999;

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id : '" + userId + "'"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testDeleteUser_caseNullId() throws Exception {
        Integer userId = null;

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isBadRequest());
    }
}