package com.draft.restapi.common.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegexHelper.class);

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
            LOGGER.error("Error extracting key with regex: {}", regex, e);
            return null;
        }
    }
}
