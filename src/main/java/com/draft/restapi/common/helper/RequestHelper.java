package com.draft.restapi.common.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RequestHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);

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
