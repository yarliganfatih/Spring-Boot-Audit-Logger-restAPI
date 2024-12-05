package com.draft.restapi.common.masking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaskUtilsTest {

    @Test
    void testMaskJsonField() {
        String jsonString = "{\"email\":\"abcd@test.com\", \"password\":\"secret\", \"username\":\"admin\"}";

        // Test FULL mask on password field
        String fullMaskedJson = MaskUtils.maskJsonField(jsonString, "password", MaskType.FULL);
        assertTrue(fullMaskedJson.contains("\"password\":\"******\""));
        assertTrue(fullMaskedJson.contains("\"username\":\"admin\"")); // to ensure no masking after password value

        // Test PARTIAL mask on username field
        String partialMaskedJson = MaskUtils.maskJsonField(jsonString, "username", MaskType.PARTIAL);
        assertTrue(partialMaskedJson.contains("\"username\":\"ad******in\""));

        // Test PARTIAL_EMAIL mask on email field
        String partialEmailMaskedJson = MaskUtils.maskJsonField(jsonString, "email", MaskType.PARTIAL_EMAIL);
        assertTrue(partialEmailMaskedJson.contains("\"email\":\"ab******d@test.com\""));

        // Test PARTIAL_AUTH mask on a hypothetical authorization field
        String authJsonString = "{\"authorization\":\"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\"}";
        String partialAuthMaskedJson = MaskUtils.maskJsonField(authJsonString, "authorization", MaskType.PARTIAL_AUTH);
        assertTrue(partialAuthMaskedJson.contains("\"authorization\":\"Bearer ******\""));

        // Test ignoring when field is missing
        String maskedJsonMissing = MaskUtils.maskJsonField(jsonString, "nonExistent", MaskType.FULL);
        assertEquals(jsonString, maskedJsonMissing); // Should remain unchanged

        // Test with spacing variations in JSON
        String sloppyJson = "{\"password\"  :   \"mySecret123\"}";
        String maskedJsonSloppy = MaskUtils.maskJsonField(sloppyJson, "password", MaskType.FULL);
        assertTrue(maskedJsonSloppy.contains("\"password\"  :   \"******\""));

        // Test with null input
        assertNull(MaskUtils.maskJsonField(null, "password", MaskType.FULL));
    }

    @Test
    void testMaskJsonField_withExactMatch() {
        String logString = "{\"userEmail\":\"abcd@test.com\", \"user-email\":\"bcda@test.com\", \"user_email\":\"cdab@test.com\"}";
        String fullMaskedLog = MaskUtils.maskJsonField(logString, "user-email", MaskType.PARTIAL_EMAIL, true);
        assertFalse(fullMaskedLog.contains("\"userEmail\":\"ab******d@test.com\""));
        assertTrue(fullMaskedLog.contains("\"user-email\":\"bc******a@test.com\""));
        assertFalse(fullMaskedLog.contains("\"user_email\":\"cd******b@test.com\""));
    }

    @Test
    void testMaskJsonField_withFuzzyMatch() {
        String logString = "{\"userEmail\":\"abcd@test.com\", \"user-email\":\"bcda@test.com\", \"user_email\":\"cdab@test.com\"}";
        String fullMaskedLog = MaskUtils.maskJsonField(logString, "user-email", MaskType.PARTIAL_EMAIL, false);
        assertTrue(fullMaskedLog.contains("\"userEmail\":\"ab******d@test.com\""));
        assertTrue(fullMaskedLog.contains("\"user-email\":\"bc******a@test.com\""));
        assertTrue(fullMaskedLog.contains("\"user_email\":\"cd******b@test.com\""));
    }

    @Test
    void testMaskJsonFields() {
        String jsonString = "{\"email\":\"abcd@test.com\", \"password\":\"secret\", \"username\":\"admin\", \"access_token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\"}";

        String allMaskedJson = MaskUtils.maskJsonFields(jsonString); // default fuzzyMatch

        assertTrue(allMaskedJson.contains("\"password\":\"******\""));
        assertTrue(allMaskedJson.contains("\"access_token\":\"******\"")); // FULL_MASKED_FIELDS has *-token
        assertTrue(allMaskedJson.contains("\"username\":\"admin\""));
        assertTrue(allMaskedJson.contains("\"email\":\"ab******d@test.com\""));

        assertEquals("", MaskUtils.maskJsonFields(""));
        assertNull(MaskUtils.maskJsonFields(null));
    }

    @Test
    void testMaskJsonFields_withWildcardAndExclusion() {
        String json = "{\"api-key\":\"secretToken\", \"key\":\"commonToken\"}";
        String maskedJson = MaskUtils.maskJsonFields(json);
        assertTrue(maskedJson.contains("\"api-key\":\"******\"")); // FULL_MASKED_FIELDS has *-key
        assertTrue(maskedJson.contains("\"key\":\"commonToken\"")); // UNMASKED_FIELDS has key
    }

    @Test
    void testMaskLogField() {
        String logString = "UserDto(email:abcd@test.com, password:secret, username:admin)";

        // Test FULL mask on password field
        String fullMaskedLog = MaskUtils.maskLogField(logString, "password", MaskType.FULL);
        assertTrue(fullMaskedLog.contains("password:******"));
        assertTrue(fullMaskedLog.contains("username:admin")); // to ensure no masking after password value

        // Test PARTIAL mask on username field
        String partialMaskedLog = MaskUtils.maskLogField(logString, "username", MaskType.PARTIAL);
        assertTrue(partialMaskedLog.contains("username:ad******in"));

        // Test PARTIAL_EMAIL mask on email field
        String partialEmailMaskedLog = MaskUtils.maskLogField(logString, "email", MaskType.PARTIAL_EMAIL);
        assertTrue(partialEmailMaskedLog.contains("email:ab******d@test.com"));

        // Test PARTIAL_AUTH mask on a hypothetical authorization field
        String authLogString = "authorization:Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String partialAuthMaskedLog = MaskUtils.maskLogField(authLogString, "authorization", MaskType.PARTIAL_AUTH);
        assertTrue(partialAuthMaskedLog.contains("authorization:Bearer ******"));

        // Test PARTIAL_AUTH mask with Basic auth
        String basicAuthLog = "authorization: Basic dXNlcjpwYXNz...";
        String basicMasked = MaskUtils.maskLogField(basicAuthLog, "authorization", MaskType.PARTIAL_AUTH);
        assertTrue(basicMasked.contains("authorization: Basic ******"));

        // Test ignoring when field is missing
        String maskedLogMissing = MaskUtils.maskLogField(logString, "nonExistent", MaskType.FULL);
        assertEquals(logString, maskedLogMissing); // Should remain unchanged

        // Test with spacing variations in Log
        String sloppyLog = "password  :   mySecret123 another word ...";
        String maskedLogSloppy = MaskUtils.maskLogField(sloppyLog, "password", MaskType.FULL);
        assertTrue(maskedLogSloppy.contains("password  :   ****** another word ..."));

        // Test with null input
        assertNull(MaskUtils.maskLogField(null, "password", MaskType.FULL));
    }

    @Test
    void testMaskLogField_withExactMatch() {
        String logString = "userEmail:abcd@test.com, user-email:bcda@test.com, user_email:cdab@test.com";
        String fullMaskedLog = MaskUtils.maskLogField(logString, "user-email", MaskType.PARTIAL_EMAIL, true);
        assertFalse(fullMaskedLog.contains("userEmail:ab******d@test.com"));
        assertTrue(fullMaskedLog.contains("user-email:bc******a@test.com"));
        assertFalse(fullMaskedLog.contains("user_email:cd******b@test.com"));
    }

    @Test
    void testMaskLogField_withFuzzyMatch() {
        String logString = "userEmail:abcd@test.com, user-email:bcda@test.com, user_email:cdab@test.com";
        String fullMaskedLog = MaskUtils.maskLogField(logString, "user-email", MaskType.PARTIAL_EMAIL, false);
        assertTrue(fullMaskedLog.contains("userEmail:ab******d@test.com"));
        assertTrue(fullMaskedLog.contains("user-email:bc******a@test.com"));
        assertTrue(fullMaskedLog.contains("user_email:cd******b@test.com"));
    }

    @Test
    void testMaskLogField_caseLogWithQuotes() {
        // Test with key 'value' format
        String logWithSingleQuotes = "authorization 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9'";
        String maskedLogWithSingleQuotes = MaskUtils.maskLogField(logWithSingleQuotes, "authorization", MaskType.PARTIAL_AUTH);
        assertTrue(maskedLogWithSingleQuotes.contains("authorization 'Bearer ******'"));

        // Test with key "value" format
        String logWithDoubleQuotes = "authorization \"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\"";
        String maskedLogWithDoubleQuotes = MaskUtils.maskLogField(logWithDoubleQuotes, "authorization", MaskType.PARTIAL_AUTH);
        assertTrue(maskedLogWithDoubleQuotes.contains("authorization \"Bearer ******\""));
    }

    @Test
    void testMaskLogFields() {
        // Test with key:value format
        String logWithColon = "UserDto(email:abcd@test.com, password:secret, username:admin)";
        String maskedColon = MaskUtils.maskLogFields(logWithColon);
        assertTrue(maskedColon.contains("password:******"));
        assertTrue(maskedColon.contains("username:admin")); 
        assertTrue(maskedColon.contains("email:ab******d@test.com"));

        // Test with key=value format
        String logString = "UserDto(email=abcd@test.com, password=secret, username=admin)";
        String maskedAll = MaskUtils.maskLogFields(logString);
        assertTrue(maskedAll.contains("password=******"));
        assertTrue(maskedAll.contains("username=admin")); 
        assertTrue(maskedAll.contains("email=ab******d@test.com"));

        assertEquals("", MaskUtils.maskLogFields(""));
        assertNull(MaskUtils.maskLogFields(null));
    }

    @Test
    void testMaskLogFields_withWildcardAndExclusion() {
        String json = "apiKey=secretToken, key:commonToken";
        String maskedJson = MaskUtils.maskLogFields(json);
        assertTrue(maskedJson.contains("apiKey=******")); // FULL_MASKED_FIELDS has *-key
        assertTrue(maskedJson.contains("key:commonToken")); // UNMASKED_FIELDS has key
    }

    @Test
    void testMaskLogFields_caseLogWithQuotes() {
        // Test for key 'value' format
        String logWithSingleQuotes = "UserDto(email 'abcd@test.com' password 'password with blank' username 'admin')";
        String maskedLogWithSingleQuotes = MaskUtils.maskLogFields(logWithSingleQuotes);
        assertTrue(maskedLogWithSingleQuotes.contains("password '******'"));
        assertTrue(maskedLogWithSingleQuotes.contains("username 'admin'"));
        assertTrue(maskedLogWithSingleQuotes.contains("email 'ab******d@test.com'"));

        // Test for key "value" format
        String logWithDoubleQuotes = "UserDto(email \"abcd@test.com\" password \"password with blank\" username \"admin\")";
        String maskedLogWithDoubleQuotes = MaskUtils.maskLogFields(logWithDoubleQuotes);
        assertTrue(maskedLogWithDoubleQuotes.contains("password \"******\""));
        assertTrue(maskedLogWithDoubleQuotes.contains("username \"admin\""));
        assertTrue(maskedLogWithDoubleQuotes.contains("email \"ab******d@test.com\""));
    }
}
