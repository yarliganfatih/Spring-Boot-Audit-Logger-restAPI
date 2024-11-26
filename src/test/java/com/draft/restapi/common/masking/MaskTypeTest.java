package com.draft.restapi.common.masking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaskTypeTest {

    @Test
    void testFullMask() {
        assertEquals("******", MaskType.FULL.mask("secret123"));
        assertEquals("", MaskType.FULL.mask(""));
        assertNull(MaskType.FULL.mask(null));
    }

    @Test
    void testPartialMask() {
        assertEquals("12******89", MaskType.PARTIAL.mask("12345689"));
        assertEquals("ab******yz", MaskType.PARTIAL.mask("abcdefyz"));
        assertEquals("******", MaskType.PARTIAL.mask("1234")); 
        assertEquals("******", MaskType.PARTIAL.mask("12")); 
        assertEquals("", MaskType.PARTIAL.mask(""));
        assertNull(MaskType.PARTIAL.mask(null));
    }

    @Test
    void testPartialEmailMask() {
        assertEquals("ab******d@test.com", MaskType.PARTIAL_EMAIL.mask("abcd@test.com"));
        assertEquals("ab******om", MaskType.PARTIAL_EMAIL.mask("abc@test.com")); 
        assertEquals("in******gn", MaskType.PARTIAL_EMAIL.mask("invalid-email-no-at-sign"));
        assertEquals("", MaskType.PARTIAL_EMAIL.mask(""));
        assertNull(MaskType.PARTIAL_EMAIL.mask(null));
    }

    @Test
    void testPartialAuthMask() {
        assertEquals("Bearer ******", MaskType.PARTIAL_AUTH.mask("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"));
        assertEquals("Bearer ******", MaskType.PARTIAL_AUTH.mask("Bearer 123"));
        assertEquals("Basic ******", MaskType.PARTIAL_AUTH.mask("Basic dXNlcjpwYXNz"));
        assertEquals("Token ******", MaskType.PARTIAL_AUTH.mask("Token 12345"));
        assertEquals("Invalid ", MaskType.PARTIAL_AUTH.mask("Invalid "));
        assertEquals("", MaskType.PARTIAL_AUTH.mask(""));
        assertNull(MaskType.PARTIAL_AUTH.mask(null));
    }

    @Test
    void testNoneMask() {
        assertEquals("plaintext", MaskType.NONE.mask("plaintext"));
        assertEquals("", MaskType.NONE.mask(""));
        assertNull(MaskType.NONE.mask(null));
    }
}
