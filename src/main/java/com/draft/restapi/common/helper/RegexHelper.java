package com.draft.restapi.common.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class RegexHelper {

    public static final String CASE_SEPERATORS = "[-_]";

    private RegexHelper() {
        // Private constructor to prevent instantiation of static helper class
    }

    public static String extractKey(String fullText, String regex, Integer groupIndex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(fullText);
            if (matcher.find()) {
                return matcher.group(groupIndex != null ? groupIndex : 1);
            }
            return null;
        } catch (Exception e) {
            log.warn("Error extracting key with regex: {}", regex, e);
            return null;
        }
    }

    public static String getWildcardPattern(String regex) {
        return regex.toLowerCase(Locale.ENGLISH).replaceAll(CASE_SEPERATORS, "").replace("*", "[\\w-]*");
    }

    public static String getJsonPattern(Set<String> fieldNames) {
        String fieldNamesPatternMatch = fieldNames.stream()
                .map(field -> field.contains("*") ? getWildcardPattern(field) : Pattern.quote(field))
                .collect(Collectors.joining("|")); // OR operator for regex
        return "(?i)(\"(" + fieldNamesPatternMatch + ")\"\\s*:\\s*\")([^\"]+)(\")";
    }

    private static final String AUTH_SCHEMES = "(?:Bearer|Basic|Digest|OAuth|Token)";

    public static String getLogPattern(Set<String> fieldNames) {
        String fieldNamesPatternMatch = fieldNames.stream()
                .map(field -> field.contains("*") ? getWildcardPattern(field) : Pattern.quote(field))
                .collect(Collectors.joining("|")); // OR operator for regex
        return "(?i)\\b(" + fieldNamesPatternMatch + ")(\\s*[=:]\\s*|\\s+)(['\"]?)(" + AUTH_SCHEMES + "\\s+[^\\s,)\\]]+|[^\\s,)\\]]+|.*?)\\3(?=[,)\\]]|\\s|$)";
    }
}
