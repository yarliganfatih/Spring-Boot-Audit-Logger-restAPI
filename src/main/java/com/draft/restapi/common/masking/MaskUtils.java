package com.draft.restapi.common.masking;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.draft.restapi.common.helper.RegexHelper;

public final class MaskUtils {

    public static final String MASKED_VALUE = "******";

    public static final Set<String> FULL_MASKED_FIELDS = new HashSet<>(Arrays.asList(
            "cookie", "set-cookie", "session", "session-id", "csrf", "csrf-token", 
            "api-key", "api-token", "access-token", "refresh-token", "auth-token", "token",
            "password", "secret", "otp", "pin", "signature", "auth", "key", "private-key", "public-key"
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
        if (jsonString == null || jsonString.isEmpty()) return jsonString;
        Matcher m = Pattern.compile(RegexHelper.getJsonPattern(fieldName)).matcher(jsonString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String originalValue = m.group(2);
            String maskedValue = maskType.mask(originalValue);
            m.appendReplacement(sb, m.group(1) + Matcher.quoteReplacement(maskedValue) + m.group(3));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String maskJsonFields(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) return jsonString;
        Matcher m = Pattern.compile(RegexHelper.getJsonPattern(ALL_MASKED_FIELDS)).matcher(jsonString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(2);
            String originalValue = m.group(3);
            String maskedValue = MaskType.by(fieldName).mask(originalValue);
            m.appendReplacement(sb, m.group(1) + Matcher.quoteReplacement(maskedValue) + m.group(4));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
