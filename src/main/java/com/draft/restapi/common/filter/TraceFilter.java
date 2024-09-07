package com.draft.restapi.common.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;

import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class TraceFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "x-traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString();
        response.addHeader("x-trace", traceId);

        MDC.put(TRACE_ID, traceId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
