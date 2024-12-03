package com.draft.restapi.common.masking;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.draft.restapi.common.enums.CasePattern;
import com.draft.restapi.common.helper.RegexHelper;

public final class MaskUtils {

    public static final String MASKED_VALUE = "******";

    public static final Set<String> FULL_MASKED_FIELDS = new HashSet<>(Arrays.asList(
            "cookie", "set-cookie", "session", "session-id", "csrf", "csrf-token", 
            "api-key", "api-token", "access-token", "refresh-token", "auth-token", "token",
            "password", "secret", "otp", "pin", "signature", "auth", "private-key", "public-key"
    ));

    public static final Set<String> PARTIAL_MASKED_FIELDS = new HashSet<>(Arrays.asList(
            "first-name", "last-name", "address", "city", "state", "zip-code", "postal-code",
            "phone-number", "mobile-number", "contact-number"
    ));

    public static final Set<String> PARTIAL_EMAIL_MASKED_FIELDS = new HashSet<>(Arrays.asList(
            "email", "email-address", "user-email", "contact-email"
    ));

    public static final Set<String> PARTIAL_AUTH_MASKED_FIELDS = new HashSet<>(Arrays.asList(
            "authorization", "proxy-authorization"
    ));

    public static final Set<String> ALL_MASKED_FIELDS = new HashSet<>();
    static {
        ALL_MASKED_FIELDS.addAll(FULL_MASKED_FIELDS);
        ALL_MASKED_FIELDS.addAll(PARTIAL_MASKED_FIELDS);
        ALL_MASKED_FIELDS.addAll(PARTIAL_AUTH_MASKED_FIELDS);
        ALL_MASKED_FIELDS.addAll(PARTIAL_EMAIL_MASKED_FIELDS);
    }

    private MaskUtils() {}

    public static String maskJsonField(String jsonString, String fieldName, MaskType maskType) {
        return maskJsonFields(jsonString, Collections.singleton(fieldName), maskType, false);
    }

    public static String maskJsonField(String jsonString, String fieldName, MaskType maskType, boolean exactMatch) {
        return maskJsonFields(jsonString, Collections.singleton(fieldName), maskType, exactMatch);
    }

    public static String maskJsonFields(String jsonString) {
        return maskJsonFields(jsonString, ALL_MASKED_FIELDS, null, false);
    }

    public static String maskJsonFields(String jsonString, Set<String> allFields, MaskType maskType, boolean exactMatch) {
        if (jsonString == null || jsonString.isEmpty()) return jsonString;
        allFields = exactMatch ? allFields : expandWithAllCases(allFields);
        Matcher m = Pattern.compile(RegexHelper.getJsonPattern(allFields)).matcher(jsonString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(2);
            String originalValue = m.group(3);
            String maskedValue = maskType != null ? maskType.mask(originalValue) : MaskType.by(fieldName).mask(originalValue);
            m.appendReplacement(sb, m.group(1) + Matcher.quoteReplacement(maskedValue) + m.group(4));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static Set<String> expandWithAllCases(Set<String> fieldNames) {
        Set<String> allFieldCases = new HashSet<>();
        allFieldCases.addAll(fieldNames);
        for (CasePattern casePattern : CasePattern.values()) {
            allFieldCases.addAll(fieldNames.stream().map(casePattern::of).collect(Collectors.toList()));
        }
        return allFieldCases;
    }

    public static String maskLogField(String logString, String fieldName, MaskType maskType) {
        return maskLogFields(logString, Collections.singleton(fieldName), maskType, false);
    }

    public static String maskLogField(String logString, String fieldName, MaskType maskType, boolean exactMatch) {
        return maskLogFields(logString, Collections.singleton(fieldName), maskType, exactMatch);
    }

    public static String maskLogFields(String logString) {
        return maskLogFields(logString, ALL_MASKED_FIELDS, null, false);
    }

    public static String maskLogFields(String logString, Set<String> allFields, MaskType maskType, boolean exactMatch) {
        if (logString == null || logString.isEmpty()) return logString;
        allFields = exactMatch ? allFields : expandWithAllCases(allFields);
        Matcher m = Pattern.compile(RegexHelper.getLogPattern(allFields)).matcher(logString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            String originalValue = m.group(4);
            String maskedValue = maskType != null ? maskType.mask(originalValue) : MaskType.by(fieldName).mask(originalValue);
            m.appendReplacement(sb, m.group(1) + m.group(2) + m.group(3) + Matcher.quoteReplacement(maskedValue) + m.group(3));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
