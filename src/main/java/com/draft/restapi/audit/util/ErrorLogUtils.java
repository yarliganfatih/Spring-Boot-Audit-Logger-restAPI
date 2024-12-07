package com.draft.restapi.audit.util;

import static com.draft.restapi.common.aspect.MethodArgumentCaptureAspect.CAPTURED_ARGS_KEY;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

import com.draft.restapi.audit.dto.ErrorLogEvent;
import com.draft.restapi.auth.entity.User;
import com.draft.restapi.common.filter.TraceFilter;
import com.draft.restapi.common.helper.RequestHelper;
import com.draft.restapi.common.masking.MaskUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ErrorLogUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static ErrorLogEvent generateEvent(Exception ex, HttpServletRequest servletRequest) {
        ErrorLogEvent errorLog = new ErrorLogEvent();
        errorLog.setTimestamp(LocalDateTime.now());
        errorLog.setTraceId(MDC.get(TraceFilter.TRACE_ID));

        User user = User.getLoggedUser();
        if (user != null) {
            errorLog.setOccurredById(user.getId());
            errorLog.setOccurredByUsername(user.getUsername());
        }

        if (ex != null) { 
            errorLog.setErrorMessage(ex.getMessage());
            errorLog.setErrorType(ex.getClass().getName());
            errorLog.setErrorStackTrace(genErrorStackTrace(ex));
        }

        if (servletRequest == null) servletRequest = RequestHelper.getServletRequest();
        if (servletRequest != null) {
            errorLog.setEndpointUrl(servletRequest.getRequestURI());
            errorLog.setHttpMethod(servletRequest.getMethod());
            errorLog.setRequestParams(servletRequest.getQueryString());
            errorLog.setMethodArguments((List<Map<String, String>>) servletRequest.getAttribute(CAPTURED_ARGS_KEY));

            try {
                errorLog.setRequestHeaders(MaskUtils.maskJsonFields(OBJECT_MAPPER.writeValueAsString(RequestHelper.getRequestHeaders(servletRequest))));
            } catch (Exception ignore) {
                errorLog.setRequestHeaders("[Unserializable Headers]");
            }

            try {
                errorLog.setRequestBody(MaskUtils.maskJsonFields(RequestHelper.getRequestBody(servletRequest)));
            } catch (Exception ignore) {
                errorLog.setRequestBody("[Unserializable Body]");
            }
        }
        return errorLog;
    }

    private static List<String> genErrorStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return Arrays.asList(sw.toString()
                .replaceAll("\tat ", "")
                .replaceAll("\t", "")
                .replaceAll("\r", "")
                .split("\n"));
    }
}
