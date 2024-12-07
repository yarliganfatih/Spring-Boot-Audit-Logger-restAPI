package com.draft.restapi.common.masking;

import java.util.Collections;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.draft.restapi.audit.dto.ErrorLogEvent;
import com.draft.restapi.audit.util.ErrorLogUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Plugin(name = "MaskingPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"maskedMsg"})
public class MaskingPatternConverter extends LogEventPatternConverter {
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ERROR_LOGGER");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    protected MaskingPatternConverter(String name, String style) {
        super(name, style);
    }

    public static MaskingPatternConverter newInstance(final String[] options) {
        return new MaskingPatternConverter("maskedMsg", "maskedMsg");
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String message = event.getMessage().getFormattedMessage();
        if (message != null) {
            try {
                message = MaskUtils.maskJsonFields(message);
                message = MaskUtils.maskLogFields(message);
            } catch (Exception ex) {
                saveMaskingErrorLog(ex, message);
            } finally {
                toAppendTo.append(message);
            }
        }
    }

    private void saveMaskingErrorLog(Exception ex, String message) {
        try {
            ErrorLogEvent errorLog = ErrorLogUtils.generateEvent(ex, null);
            errorLog.addMethodSignature("MaskingPatternConverter.format(..)", Collections.singletonMap("message", message));
            ERROR_LOGGER.error(OBJECT_MAPPER.writeValueAsString(errorLog)); // do not mask here
        } catch (Exception ignore) {
            // do not log here, it may cause infinite loop
        }
    }
}
