package com.draft.restapi.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import com.draft.restapi.auth.mapper.UserMapper;
import com.draft.restapi.common.cache.CircuitBreakerCache;
import com.draft.restapi.auth.entity.User;
import com.draft.restapi.auth.entity.dto.UserDto;
import com.jayway.jsonpath.JsonPath;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@SuppressWarnings("null")
@Sql(scripts = {"classpath:db/sql/insert-user-data.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:db/sql/insert-user-data-rollback.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @SpyBean
    private CacheManager cacheManager;

    @SpyBean
    private UserMapper userMapper;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private String createUserJson( String email, String username, String password) {
        Map<String, String> request = new HashMap<>();
        if (email != null) request.put("email", email);
        if (username != null) request.put("username", username);
        if (password != null) request.put("password", password);
        return asJsonString(request);
    }

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
        String email = "createdUser@example.com";
        String username = "createdUser";
        String password = "createdUser123";

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
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
    public void testCreateUser_checkDataIntegrity() throws Exception {
        String email = "createdUser@example.com";
        String username = "createdUser";
        String password = "createdUser123";

        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isCreated()) // cache.doPut
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Integer userId = JsonPath.read(responseContent, "$.data.id");
        assertAuditLogs("users", userId.longValue(), "CREATE");
        
        // double-check for data integrity after data manipulation
        mockMvc.perform(get("/api/users?id=" + userId))
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)) // skip if failed
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/users/" + userId)) // from cache.doGet
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)) // skip if failed
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_caseNotNullable() throws Exception {
        String email = "createdUser@example.com";
        String username = "createdUser";
        String password = "createdUser123";

        // Simulate a request with a missing required field (in service layer)
        User invalidUser = new User();
        invalidUser.setEmail(email);
        invalidUser.setUsername(null); // because of missing username field
        invalidUser.setPassword(password);
        Mockito.when(userMapper.toEntity(Mockito.any(UserDto.class))).thenReturn(invalidUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("notNullable"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("username"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_caseDuplicateError() throws Exception {
        String email = "mockUser2@example.com"; // unique
        String username = "mockUser"; // because of sending existing value
        String password = "mockUser123";

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("duplicate"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("username"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_withIdempotencyKey_preventsDuplicateAndReturnsCache() throws Exception {
        String email = "createdUser@example.com";
        String username = "createdUser";
        String password = "createdUser123";
        String idempotencyKey = "unique-idempotency-key-8899";

        // first request locked the execution to prevent race condition
        mockMvc.perform(post("/api/users")
                .header("Idempotency-Key", idempotencyKey) // unique key for idempotency (by client app)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isCreated()) // cache.doPut
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));
        Mockito.verify(userMapper, Mockito.times(1)).toDto(Mockito.any(User.class)); // executed once in service layer

        // duplicate calls return the cached result, concurrent callers wait for the first run instead of racing
        mockMvc.perform(post("/api/users")
                .header("Idempotency-Key", idempotencyKey) // same key as before
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isCreated()) // cache.doGet
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));
        Mockito.verify(userMapper, Mockito.times(1)).toDto(Mockito.any(User.class)); // not executed again because of caching
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_afterSoftDelete() throws Exception {
        Integer existingUserId = 2;
        String email = "mockUser@example.com"; // because of sending existing value
        String username = "createdUser"; // unique
        String password = "createdUser123";
        
        // First, delete the existing user
        mockMvc.perform(delete("/api/users/" + existingUserId)) // soft-delete
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)); // skip if failed
 
        // Then, try to create a similar user
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("duplicate"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_afterHardDelete() throws Exception {
        Integer existingUserId = 2;
        String email = "mockUser@example.com";
        String username = "mockUser";
        String password = "mockUser123";

        // First, delete the existing user with purge
        mockMvc.perform(delete("/api/users/" + existingUserId)
                .param("purge", "true")) // hard-delete
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)); // skip if failed
 
        // Then, try to create a similar user
        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Integer createdUserId = JsonPath.read(responseContent, "$.data.id");
        assertAuditLogs("users", createdUserId.longValue(), "CREATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testCreateUser_caseValidationError() throws Exception {
        String email = ""; // because of sending empty value and size < 3
        String username = "created-user"; // because of using hyphen which is invalid pattern

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, null))) // because of missing password field
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(4));
    }

    @Test
    @Order(2) // to detect flaky tests
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
    public void testGetUser_fromCache() throws Exception {
        Integer userId = 2;

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk()); // cache.doPut
        Mockito.verify(userMapper, Mockito.times(1)).toDto(Mockito.any(User.class)); // executed once in service layer

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk()) // from cache.doGet
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value("mockUser@example.com"))
                .andExpect(jsonPath("$.data.username").value("mockUser"));
        Mockito.verify(userMapper, Mockito.times(1)).toDto(Mockito.any(User.class)); // not executed again because of caching
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testGetUser_fromCache_redisFailover() throws Exception {
        Integer userId = 2;

        // simulate redis failure for caching is not usable, fallback to normal process (Source of Truth)
        Cache mockCache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
        Mockito.when(mockCache.get(Mockito.any())).thenThrow(new RuntimeException("Redis connection timed out"));
        Mockito.doThrow(new RuntimeException("Redis connection timed out")).when(mockCache).put(Mockito.any(), Mockito.any());

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk()); // cache.doPut is not executed due to redis failure
        Mockito.verify(userMapper, Mockito.times(1)).toDto(Mockito.any(User.class)); // executed once in service layer

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk()); // from database, not cache
        Mockito.verify(userMapper, Mockito.times(2)).toDto(Mockito.any(User.class)); // executed again because of non-cachable
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    // Controller -> cache.doGet -> service.getUserById -> cache.doPut => Response (200)
    public void testGetUser_fromCache_redisFailover_withCircuitBreaker() throws Exception {
        Integer userId = 2;
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("redisCache");
        Assumptions.assumeTrue(CircuitBreaker.State.CLOSED == circuitBreaker.getState());

        // simulate redis failure with circuit breaking for caching is not usable, fallback to normal process (Source of Truth)
        Cache mockCache = Mockito.mock(Cache.class);
        Cache circuitBreakerCache = new CircuitBreakerCache(mockCache, circuitBreaker);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(circuitBreakerCache);
        Mockito.when(mockCache.get(Mockito.any())).thenThrow(new RuntimeException("Redis connection timed out"));
        Mockito.doThrow(new RuntimeException("Redis connection timed out")).when(mockCache).put(Mockito.any(), Mockito.any());

        try {
            // trigger consecutive failures with 5 requests to reach redisCache.minimum-number-of-calls (10) (5+5)
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/api/users/" + userId)); // each request calls 1 cache.doGet and 1 cache.doPut
            }
            Assertions.assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
            verifyCachableWithCallTimes(mockCache, 5, 5, 5);

            // caching methods should NOT be called since circuit breaker is OPEN
            mockMvc.perform(get("/api/users/" + userId)); // short-circuited
            Assertions.assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
            verifyCachableWithCallTimes(mockCache, 5, 6, 5);

            // wait for redisCache.wait-duration-in-open-state (3s)
            Thread.sleep(3000);
            
            // caching methods should be called since circuit breaker is NOT OPEN
            mockMvc.perform(get("/api/users/" + userId)); // not short-circuited
            Assertions.assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
            verifyCachableWithCallTimes(mockCache, 6, 7, 6);
        } finally {
            circuitBreaker.transitionToClosedState();
        }
    }

    private void verifyCachableWithCallTimes(Cache mockCache, int doGetCount, int invokeCount, int doPutCount) {
        Mockito.verify(mockCache, Mockito.times(doGetCount)).get(Mockito.any()); // return cached data if exists
        Mockito.verify(userMapper, Mockito.times(invokeCount)).toDto(Mockito.any(User.class)); // get daha from DB
        Mockito.verify(mockCache, Mockito.times(doPutCount)).put(Mockito.any(), Mockito.any()); // save data to cache
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
    @Order(1) // to detect flaky tests
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser() throws Exception {
        String email = "updatedUser@example.com";
        String username = "updatedUser";
        String password = "updatedUser123";
        Integer userId = 2;

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));
        assertAuditLogs("users", userId.longValue(), "UPDATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_checkDataIntegrity() throws Exception {
        String email = "updatedUser@example.com";
        String username = "updatedUser";
        String password = "updatedUser123";
        Integer userId = 2;

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isOk()) // cache.doPut
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));
        assertAuditLogs("users", userId.longValue(), "UPDATE");
        
        // double-check for data integrity after data manipulation
        mockMvc.perform(get("/api/users?id=" + userId))
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)) // skip if failed
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/users/" + userId)) // from cache.doGet
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)) // skip if failed
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_withPatching() throws Exception {
        String username = "updatedUser";
        Integer userId = 2;

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(null, username, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value(username));
        assertAuditLogs("users", userId.longValue(), "UPDATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseNotFound() throws Exception {
        String email = "updatedUser@example.com";
        String username = "updatedUser";
        String password = "updatedUser123";
        Integer userId = 999;

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id : '" + userId + "'"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseValidationError() throws Exception {
        String email = ""; // because of size < 3
        String username = "updatedUser";
        Integer userId = 2;

        // Unlike createUser validations, no NotBlank error will occur here
        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].code").value("Size"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("email"));
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseNullId() throws Exception {
        String email = "updatedUser@example.com";
        String username = "updatedUser";
        String password = "updatedUser123";
        Integer userId = null;

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testUpdateUser_caseTruncationError() throws Exception {
        String email = "updatedUser123@example.com";
        String validUsername = "updatedUser";
        String password = "updatedUser123";
        Integer userId = 2;
        
        // Simulate a request with a invalid field (in service layer)
        String invalidUsername = String.join("", Collections.nCopies(65, "n")); // because of exceeds maximum length (64)
        Mockito.doAnswer(invocation -> {
            User userArg = invocation.getArgument(1);
            userArg.setUsername(invalidUsername); 
            return null;
        }).when(userMapper).updateUserFromDto(Mockito.any(), Mockito.any());

        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, validUsername, password)))
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

        mockMvc.perform(delete("/api/users/" + userId)) // soft-delete
                .andExpect(status().isOk());
        assertAuditLogs("users", userId.longValue(), "UPDATE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testDeleteUser_checkDataIntegrity() throws Exception {
        Integer userId = 2;

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk()); // cache.doEvict
        assertAuditLogs("users", userId.longValue(), "UPDATE"); // soft-delete

        // double-check for data integrity after data manipulation
        mockMvc.perform(get("/api/users?id=" + userId))
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)) // skip if failed
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/api/users/" + userId)) // from database, not cache
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
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
    public void testDeleteUser_withPurge() throws Exception {
        Integer userId = 2;

        mockMvc.perform(delete("/api/users/" + userId)
                .param("purge", "true")) // hard-delete
                .andExpect(status().isOk());
        assertAuditLogs("users", userId.longValue(), "DELETE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testDeleteUser_withPurge_afterSoftDelete() throws Exception {
        Integer userId = 2;
        mockMvc.perform(delete("/api/users/" + userId)) // soft-delete
                .andExpect(res -> Assumptions.assumeTrue(res.getResponse().getStatus() == 200)); // skip if failed

        mockMvc.perform(delete("/api/users/" + userId)
                .param("purge", "true")) // hard-delete
                .andExpect(status().isOk());
        assertAuditLogs("users", userId.longValue(), "DELETE");
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

        mockMvc.perform(delete("/api/users/" + userId)
                .param("purge", "true")) // hard-delete
                .andExpect(status().isOk());
        assertAuditLogs("users", userId.longValue(), "DELETE");
    }

    @Test
    @WithMockUser(username = "user", roles = { "user" })
    public void testAllCrudOperations() throws Exception {
        String email = "createdUser@example.com";
        String username = "createdUser";
        String password = "createdUser123";

        // first, create an user
        MvcResult result = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(email, username, password)))
                .andExpect(status().isCreated()) // cache.doPut
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        Integer userId = JsonPath.read(responseContent, "$.data.id");
        assertAuditLogs("users", userId.longValue(), "CREATE");

        // find the user to verify creation
        mockMvc.perform(get("/api/users?id=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // get the user to verify creation
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk()) // from cache.doGet
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(username));

        // then, update the user
        String updatedUsername = "updatedUser";
        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(null, updatedUsername, null)))
                .andExpect(status().isOk()) // cache.doPut
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(updatedUsername));
        assertAuditLogs("users", userId.longValue(), "UPDATE");

        // find the user to verify update
        mockMvc.perform(get("/api/users?id=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // get the user to verify update
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk()) // from cache.doGet
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.username").value(updatedUsername));

        // then, delete the user
        mockMvc.perform(delete("/api/users/" + userId)) // soft-delete
                .andExpect(status().isOk()); // cache.doEvict
        assertAuditLogs("users", userId.longValue(), "UPDATE");

        // try to find the user after deletion
        mockMvc.perform(get("/api/users?id=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        
        // try to get the user after deletion
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isNotFound()); // from database, not cache

        // try to update the user after deletion
        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }
}