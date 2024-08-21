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
public class UserControllerTest extends BaseControllerTest {

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
        String userJson = "{\"email\": \"" + username + "@example.com\", \"username\": \"" + username + "\", \"password\": \"" + username + "\"}";

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(username + "@example.com"))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Integer userId = JsonPath.read(responseContent, "$.id");
        assertAuditLogs("users", userId.longValue(), "CREATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetUser() throws Exception {
        Integer userId = 2;

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("mockUser@example.com"))
                .andExpect(jsonPath("$.username").value("mockUser"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser() throws Exception {
        String username = "updatedUser";
        Integer userId = 2;
        String userJson = "{\"email\": \"" + username + "@example.com\", \"username\": \"" + username + "\", \"password\": \"" + username + "\"}";

        mockMvc.perform(put("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(username + "@example.com"))
                .andExpect(jsonPath("$.username").value(username));
        assertAuditLogs("users", userId.longValue(), "UPDATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testDeleteUser() throws Exception {
        Integer userId = 2;

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk());
        assertAuditLogs("users", userId.longValue(), "DELETE");
    }
}