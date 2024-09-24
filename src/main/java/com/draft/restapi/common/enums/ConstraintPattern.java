package com.draft.restapi.common.enums;

public enum ConstraintPattern {
    UNIQUE_KEY("uk_%s_unique_%s_key");

    private final String pattern;

    ConstraintPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String getRegexPattern() {
        return pattern.replaceAll("%s", "(.*?)");
    }
}