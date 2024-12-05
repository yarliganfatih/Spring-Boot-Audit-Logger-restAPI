package com.draft.restapi.common.masking;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.draft.restapi.auth.entity.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
* Tests for logging sensitive data with masking
*
* verification rules:
*   case#1: sensitive data should not be logged
*   case#2: sensitive data must be masked if they were logged
*   case#3: remaining parts must not be affected in log message
*/
@ExtendWith(OutputCaptureExtension.class)
public class MaskingPatternConverterTest {

    private MockService mockService = new MockService();

    @BeforeAll
    static void setupLog4j() {
        System.setProperty("log4j.configurationFile", "classpath:log4j2-spring.xml");
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.setConfigLocation(URI.create("classpath:log4j2-spring.xml"));
        context.reconfigure();
    }

    @Test
    void testLogObjectToString(CapturedOutput output) {
        mockService.logObjectToString();
        assertFalse(output.getOut().contains("password=secret"));
        assertTrue(output.getOut().contains("email=ab******d@test.com"));
        assertTrue(output.getOut().contains("username=admin"));
    }

    @Test
    void testLogObjectToJson(CapturedOutput output) throws Exception {
        mockService.logObjectToJson();
        assertFalse(output.getOut().contains("\"password\":\"secret\""));
        assertTrue(output.getOut().contains("\"email\":\"ab******d@test.com\""));
        assertTrue(output.getOut().contains("\"username\":\"admin\""));
    }

    @Test
    void testLogSensitiveData(CapturedOutput output) {
        mockService.logSensitiveData();
        assertFalse(output.getOut().contains("authorization: 'Bearer jwtToken'"));
        assertTrue(output.getOut().contains("authorization: 'Bearer ******'"));
        assertTrue(output.getOut().contains("User has authorization: 'Bearer ******' for access restAPI."));
    }

    @Test
    void testLogSensitiveData_withMasking(CapturedOutput output) {
        mockService.logSensitiveData_withMasking();
        assertFalse(output.getOut().contains("authorization: 'Bearer jwtToken'"));
        assertTrue(output.getOut().contains("authorization: 'Bearer ******'"));
        assertTrue(output.getOut().contains("User has authorization: 'Bearer ******' for access restAPI."));
    }

    @Slf4j
    static class MockService {
        String authorization = "Bearer jwtToken";
        UserDto userDto = UserDto.builder()
                .email("abcd@test.com")
                .password("secret")
                .username("admin")
                .build();

        public void logObjectToString() {
            log.info("created a user with data: {}", userDto);
        }

        public void logObjectToJson() throws Exception {
            log.info("created a user with data: {}", new ObjectMapper().writeValueAsString(userDto));
        }

        public void logSensitiveData() {
            log.info("User has authorization: '{}' for access restAPI.", authorization);
        }

        public void logSensitiveData_withMasking() {
            log.info("User has authorization: '{}' for access restAPI.", MaskType.PARTIAL_AUTH.mask(authorization));
        }
    }
}
