package com.draft.restapi.common.masking;

import static com.draft.restapi.common.masking.MaskUtils.MASKED_VALUE;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import com.draft.restapi.common.enums.CasePattern;
import com.draft.restapi.common.helper.RegexHelper;

public enum MaskType {
    NONE(MaskUtils.UNMASKED_FIELDS) { // to match excluded fields firstly
        @Override
        public String mask(String value) {
            return value;
        }
    },
    FULL(MaskUtils.FULL_MASKED_FIELDS) {
        @Override
        public String mask(String value) {
            return (value == null || value.isEmpty()) ? value : MASKED_VALUE;
        }
    },
    PARTIAL(MaskUtils.PARTIAL_MASKED_FIELDS) {
        @Override
        public String mask(String value) {
            if (value == null || value.isEmpty()) return value;
            if (value.length() <= 4) return MASKED_VALUE;
            return value.replaceAll("(?<=^.{2}).*(?=.{2}$)", MASKED_VALUE);
        }
    },
    PARTIAL_EMAIL(MaskUtils.PARTIAL_EMAIL_MASKED_FIELDS) {
        @Override
        public String mask(String value) {
            if (value == null || value.indexOf("@") < 4) return PARTIAL.mask(value);
            return value.replaceAll("(?<=^.{2})[^@]+(?=[^@]@)", MASKED_VALUE);
        }
    },
    PARTIAL_AUTH(MaskUtils.PARTIAL_AUTH_MASKED_FIELDS) {
        @Override
        public String mask(String value) {
            if (value == null || !value.contains(" ")) return PARTIAL.mask(value);
            return value.replaceAll("(?<=^\\S+\\s)\\S+", MASKED_VALUE);            
        }
    };

    public Set<String> maskedFields = Collections.emptySet();

    public abstract String mask(String value);

    MaskType() {
    }

    MaskType(Set<String> maskedFields) {
        this.maskedFields = maskedFields;
    }

    public boolean contains(String fieldName) {
        for (String maskedField : this.maskedFields) {
            boolean isMatch = maskedField.contains("*")
                    ? fieldName.toLowerCase(Locale.ENGLISH).matches("^" + RegexHelper.getWildcardPattern(maskedField) + "$")
                    : CasePattern.equalsIgnoreCase(maskedField, fieldName);
            if (isMatch) return true;
        }
        return false;
    }

    public static MaskType by(String fieldName) {
        if (fieldName == null) return NONE;
        for (MaskType maskType : MaskType.values()) {
            if (maskType.contains(fieldName)) {
                return maskType;
            }
        }
        return NONE;
    }
}
