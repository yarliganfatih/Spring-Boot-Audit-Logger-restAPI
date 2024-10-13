package com.draft.restapi.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import com.draft.restapi.auth.mapper.UserMapper;
import com.draft.restapi.auth.entity.User;
import com.draft.restapi.auth.entity.dto.UserDto;
import com.jayway.jsonpath.JsonPath;

@Sql(scripts = {"classpath:db/sql/insert-user-data.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:db/sql/insert-user-data-rollback.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @SpyBean
    private UserMapper userMapper;

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetAllUsers_withPagination() throws Exception {
        mockMvc.perform(get("/api/users?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.page.pageNumber").value(0))
                .andExpect(jsonPath("$.page.pageSize").value(1));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetAllUsers_withSorting() throws Exception {
        mockMvc.perform(get("/api/users?sort=username,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("mockUser"))
                .andExpect(jsonPath("$.data[1].username").value("user"));
        mockMvc.perform(get("/api/users?sort=username,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("user"))
                .andExpect(jsonPath("$.data[1].username").value("mockUser"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetAllUsers_caseSortingPropertyReference() throws Exception {
        mockMvc.perform(get("/api/users?sort=invalidParam,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("invalidProperty"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("sort"))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value("invalidParam"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetAllUsers_withFilter() throws Exception {
        mockMvc.perform(get("/api/users?email=mockUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value("mockUser@example.com"));
        mockMvc.perform(get("/api/users?username=uSeR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("user"));
        mockMvc.perform(get("/api/users?id=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
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
    public void testCreateUser_caseNotNullable() throws Exception {
        User validUser = new User();
        validUser.setUsername("createdUser");
        validUser.setEmail("createdUser@example.com");
        validUser.setPassword("createdUser123");
        String userJson = "{\"email\": \"" + validUser.getEmail() + "\", \"username\": \"" + validUser.getUsername() + "\", \"password\": \"" + validUser.getPassword() + "\"}";

        // Simulate a request with a missing required field (in service layer)
        User invalidUser = new User(validUser);
        invalidUser.setUsername(null);
        Mockito.when(userMapper.toEntity(Mockito.any(UserDto.class))).thenReturn(invalidUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("notNullable"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("username"));
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
        String email = ""; // because of sending empty value and size < 3
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\"}"; // because of missing password field

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(4));
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

        mockMvc.perform(patch("/api/users/" + userId)
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
    public void testUpdateUser_withPatching() throws Exception {
        String username = "updatedUser";
        Integer userId = 2;
        String userJson = "{\"username\": \"" + username + "\"}";

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
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

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id : '" + userId + "'"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseValidationError() throws Exception {
        String username = "updatedUser";
        String email = ""; // because of size < 3
        Integer userId = 2;
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\"}";

        // Unlike createUser validations, no NotBlank error will occur here
        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("Size"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseNullId() throws Exception {
        String username = "updatedUser";
        String email = username + "@example.com";
        String password = username + "123";
        Integer userId = null;
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseTruncationError() throws Exception {
        String invalidUsername = String.join("", Collections.nCopies(65, "n")); // because of exceeds maximum length (64)
        String validUsername = "updatedUser";
        String email = "updatedUser123@example.com";
        String password = "updatedUser123";
        Integer userId = 2;
        String userJson = "{\"email\": \"" + email + "\", \"username\": \"" + validUsername + "\", \"password\": \"" + password + "\"}";
        
        // Simulate a request with a invalid field (in service layer)
        Mockito.doAnswer(invocation -> {
            User userArg = invocation.getArgument(1);
            userArg.setUsername(invalidUsername); 
            return null;
        }).when(userMapper).updateUserFromDto(Mockito.any(), Mockito.any());

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("dataTruncation"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("username"));
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

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    @Sql(scripts = {"classpath:db/sql/insert-user-data.sql", "classpath:db/sql/insert-user-role-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:db/sql/insert-user-role-data-rollback.sql", "classpath:db/sql/insert-user-data-rollback.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDeleteUser_caseReferenceHandling() throws Exception {
        Integer userId = 2;

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk());
        assertAuditLogs("users", userId.longValue(), "DELETE");
    }
}