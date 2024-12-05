package com.draft.restapi.common.masking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaskTypeTest {

    @Test
    void testBy() {
        assertEquals(MaskType.NONE, MaskType.by("key"));                    // by UNMASKED_FIELDS
        assertEquals(MaskType.FULL, MaskType.by("cookie"));                 // by FULL_MASKED_FIELDS
        assertEquals(MaskType.PARTIAL, MaskType.by("first-name"));          // by PARTIAL_MASKED_FIELDS
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("email"));         // by PARTIAL_EMAIL_MASKED_FIELDS
        assertEquals(MaskType.PARTIAL_AUTH, MaskType.by("authorization"));  // by PARTIAL_AUTH_MASKED_FIELDS
        assertEquals(MaskType.NONE, MaskType.by("nonExistent"));            // unmatched
        assertEquals(MaskType.NONE, MaskType.by(""));                       // unmatched
        assertEquals(MaskType.NONE, MaskType.by(null));                     // unmatched
    }

    @Test
    void testBy_withWildcardMatch() {
        assertEquals(MaskType.NONE, MaskType.by("idempotency-key")); // excluded masked field
        assertEquals(MaskType.FULL, MaskType.by("access-token"));
        assertEquals(MaskType.FULL, MaskType.by("encrypted-access-token"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("email"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("eMail"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("e_mail"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("email-address"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("user-email"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("user-email-address"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("user_email"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("userEmail"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("user-e-mail"));
        assertEquals(MaskType.PARTIAL_EMAIL, MaskType.by("user_eMail"));
    }

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
