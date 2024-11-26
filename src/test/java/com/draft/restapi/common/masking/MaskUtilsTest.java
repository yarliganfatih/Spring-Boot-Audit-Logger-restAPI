package com.draft.restapi.common.masking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaskUtilsTest {

    @Test
    void testMaskJsonField() {
        String jsonString = "{\"username\":\"admin\", \"password\":\"secret\", \"email\":\"abcd@test.com\"}";

        // Test FULL mask on password field
        String fullMaskedJson = MaskUtils.maskJsonField(jsonString, "password", MaskType.FULL);
        assertTrue(fullMaskedJson.contains("\"password\":\"******\""));
        assertTrue(fullMaskedJson.contains("\"username\":\"admin\"")); 

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
    void testMaskJsonFields() {
        String jsonString = "{\"username\":\"admin\", \"password\":\"secret\", \"cookie\":\"JSESSIONID=123\", \"email\":\"abcd@test.com\"}";

        String allMaskedJson = MaskUtils.maskJsonFields(jsonString);

        assertTrue(allMaskedJson.contains("\"password\":\"******\""));
        assertTrue(allMaskedJson.contains("\"cookie\":\"******\""));
        assertTrue(allMaskedJson.contains("\"username\":\"admin\""));
        assertTrue(allMaskedJson.contains("\"email\":\"ab******d@test.com\""));

        assertEquals("", MaskUtils.maskJsonFields(""));
        assertNull(MaskUtils.maskJsonFields(null));
    }
}
