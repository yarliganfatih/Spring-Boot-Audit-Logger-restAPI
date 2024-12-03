package com.draft.restapi.common.masking;

import static com.draft.restapi.common.masking.MaskUtils.MASKED_VALUE;

import java.util.Collections;
import java.util.Set;

import com.draft.restapi.common.enums.CasePattern;

public enum MaskType {
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
    },
    NONE {
        @Override
        public String mask(String value) {
            return value;
        }
    };

    public Set<String> maskedFields = Collections.emptySet();

    public abstract String mask(String value);

    MaskType() {
    }

    MaskType(Set<String> maskedFields) {
        this.maskedFields = maskedFields;
    }

    public static MaskType by(String fieldName) {
        if (fieldName == null) return NONE;
        for (MaskType type : MaskType.values()) {
            if (type.maskedFields.stream().anyMatch(maskedField -> CasePattern.equalsIgnoreCase(maskedField, fieldName))) {
                return type;
            }
        }
        return NONE;
    }
}
