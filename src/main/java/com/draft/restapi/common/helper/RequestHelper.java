package com.draft.restapi.common.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RequestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static final Set<String> HIDDEN_HEADERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key", "api-key", "x-auth-token",
            "accept", "accept-language", "accept-encoding", "host", "connection", "user-agent", "origin", "cache-control", 
            "pragma", "upgrade-insecure-requests", "dnt", "if-none-match", "if-modified-since"
            // needs "content-length" and "content-type" to trace request body
            // needs "referer" to trace client source
    )));

    private RequestHelper() {
        // Private constructor to prevent instantiation of static helper class
    }

    public static Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headersMap.put(headerName, request.getHeader(headerName));
            }
        }
        return headersMap;
    }

    public static String simplifyHeaders(String requestHeaders) {
        try {
            Map<String, Object> simplifiedHeaders = OBJECT_MAPPER.readValue(requestHeaders, Map.class);
            simplifiedHeaders.keySet().removeIf(key -> {
                return HIDDEN_HEADERS.contains(key.toLowerCase()) || key.toLowerCase().startsWith("sec-");
            });
            return OBJECT_MAPPER.writeValueAsString(simplifiedHeaders);
        } catch (Exception e) {
            LOGGER.warn("Failed to serialize simplified headers", e);
            return requestHeaders;
        }
    }

    public static String getRequestBody(HttpServletRequest request) throws UnsupportedEncodingException {
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, wrapper.getCharacterEncoding());
            }
        }
        return null;
    }
}
