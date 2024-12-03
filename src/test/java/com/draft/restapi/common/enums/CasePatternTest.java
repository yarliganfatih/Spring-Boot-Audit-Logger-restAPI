package com.draft.restapi.common.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class CasePatternTest {

    @Test
    void testBy() {
        assertEquals(CasePattern.FLAT_CASE, CasePattern.by("fieldname"));
        assertEquals(CasePattern.UPPER_FLAT_CASE, CasePattern.by("FIELDNAME"));
        assertEquals(CasePattern.PASCAL_CASE, CasePattern.by("FieldName"));
        assertEquals(CasePattern.CAMEL_CASE, CasePattern.by("fieldName"));
        assertEquals(CasePattern.SNAKE_CASE, CasePattern.by("field_name"));
        assertEquals(CasePattern.SCREAMING_SNAKE_CASE, CasePattern.by("FIELD_NAME"));
        assertEquals(CasePattern.KEBAB_CASE, CasePattern.by("field-name"));
        assertEquals(CasePattern.COBOL_CASE, CasePattern.by("FIELD-NAME"));
        assertEquals(CasePattern.NONE, CasePattern.by("field name with spaces"));
    }

    @ParameterizedTest
    @CsvSource({"field_name", "FIELD_NAME", "field-name", "FIELD-NAME"}) // parsable cases
    void testOf(String fieldName) {
        assertEquals("fieldname", CasePattern.FLAT_CASE.of(fieldName));
        assertEquals("FIELDNAME", CasePattern.UPPER_FLAT_CASE.of(fieldName));
        assertEquals("FieldName", CasePattern.PASCAL_CASE.of(fieldName));
        assertEquals("fieldName", CasePattern.CAMEL_CASE.of(fieldName));
        assertEquals("field_name", CasePattern.SNAKE_CASE.of(fieldName));
        assertEquals("FIELD_NAME", CasePattern.SCREAMING_SNAKE_CASE.of(fieldName));
        assertEquals("field-name", CasePattern.KEBAB_CASE.of(fieldName));
        assertEquals("FIELD-NAME", CasePattern.COBOL_CASE.of(fieldName));
    }

    @ParameterizedTest
    @CsvSource({"fieldname", "FIELDNAME", "FieldName", "fieldName", "field_name", "FIELD_NAME", "field-name", "FIELD-NAME"})
    void testEqualsIgnoreCase(String fieldName) {
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "fieldname"));
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "FIELDNAME"));
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "FieldName"));
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "fieldName"));
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "field_name"));
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "FIELD_NAME"));
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "field-name"));
        assertTrue(CasePattern.equalsIgnoreCase(fieldName, "FIELD-NAME"));
    }

    @Test
    void testEqualsIgnoreCase_False() {
        assertFalse(CasePattern.equalsIgnoreCase("field-name", "anotherField"));
        assertFalse(CasePattern.equalsIgnoreCase(null, "fieldName"));
        assertFalse(CasePattern.equalsIgnoreCase("fieldName", null));
        assertFalse(CasePattern.equalsIgnoreCase(null, null));
    }
}
