package com.draft.restapi.common.enums;

public enum ConstraintPattern {
    FOREIGN_KEY("fk_%s_foreign_%s_key"),
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