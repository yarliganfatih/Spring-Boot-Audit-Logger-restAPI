package com.draft.restapi.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.draft.restapi.common.helper.RegexHelper;

public enum CasePattern {
    FLAT_CASE("^[a-z]+([a-z0-9]+)*$", "", false),                   // flatcase
    UPPER_FLAT_CASE("^[A-Z]+([A-Z0-9]+)*$", "", true),              // UPPERFLATCASE
    PASCAL_CASE("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$", "", false),    // PascalCase
    CAMEL_CASE("^[a-z]+([A-Z][a-z0-9]+)*$", "", false),             // camelCase
    SNAKE_CASE("^[a-z]+(_[a-z0-9]+)*$", "_", false),                // snake_case
    SCREAMING_SNAKE_CASE("^[A-Z]+(_[A-Z0-9]+)*$", "_", true),       // SCREAMING_SNAKE_CASE
    KEBAB_CASE("^[a-z]+(-[a-z0-9]+)*$", "-", false),                // kebab-case
    COBOL_CASE("^[A-Z]+(-[A-Z0-9]+)*$", "-", true),                 // COBOL-CASE
    NONE("", "", false);

    private final String pattern;
    private final String separator;
    private final boolean hasUpperCase;

    CasePattern(String pattern, String separator, boolean hasUpperCase) {
        this.pattern = pattern;
        this.separator = separator;
        this.hasUpperCase = hasUpperCase;
    }

    public boolean matches(String input) {
        return input != null && input.matches(pattern);
    }

    public String of(List<String> words) {
        if (words == null || words.isEmpty()) return "";
        if (hasUpperCase) {
            words = words.stream().map(word -> word.toUpperCase(Locale.ENGLISH)).collect(Collectors.toList());
        } else {
            words = words.stream().map(word -> word.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
        }
        if (this == PASCAL_CASE || this == CAMEL_CASE) {
            words = words.stream().map(StringUtils::capitalize).collect(Collectors.toList());
        }
        if (this == CAMEL_CASE) {
            words.set(0, words.get(0).toLowerCase(Locale.ENGLISH));
        }
        return String.join(separator, words);
    }

    public String of(String input) {
        if (input == null || input.isEmpty()) return input;
        List<String> words = Arrays.asList(input.split(RegexHelper.CASE_SEPERATORS));
        return this.of(words);
    }

    public static CasePattern by(String input) {
        for (CasePattern casePattern : CasePattern.values()) {
            if (casePattern.matches(input)) {
                return casePattern;
            }
        }
        return NONE;
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null || str2 == null) return false;
        return FLAT_CASE.of(str1).equals(FLAT_CASE.of(str2));
    }
}
