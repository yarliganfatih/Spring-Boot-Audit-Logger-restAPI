package com.draft.restapi.common.masking;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

@Plugin(name = "MaskingPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"maskedJson"})
public class MaskingPatternConverter extends LogEventPatternConverter {

    protected MaskingPatternConverter(String name, String style) {
        super(name, style);
    }

    public static MaskingPatternConverter newInstance(final String[] options) {
        return new MaskingPatternConverter("maskedJson", "maskedJson");
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String message = event.getMessage().getFormattedMessage();
        if (message != null) {
            message = MaskUtils.maskJsonFields(message);
            toAppendTo.append(message);
        }
    }
}
