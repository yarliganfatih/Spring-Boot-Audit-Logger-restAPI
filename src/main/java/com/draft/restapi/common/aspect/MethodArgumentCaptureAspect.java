package com.draft.restapi.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class MethodArgumentCaptureAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodArgumentCaptureAspect.class);

    public static final String CAPTURED_ARGS_KEY = "capturedMethodArgs";

    @AfterThrowing(pointcut = "within(com.draft.restapi..controller..*) || within(com.draft.restapi..service..*)", throwing = "ex")
    public void captureMethodArgumentsOnException(JoinPoint joinPoint, Throwable ex) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            List<Map<String, String>> capturedMethods = new ArrayList<>();
            if (attributes.getRequest().getAttribute(CAPTURED_ARGS_KEY) != null) {
                capturedMethods = (List<Map<String, String>>) attributes.getRequest().getAttribute(CAPTURED_ARGS_KEY);
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] paramArgs = joinPoint.getArgs();

            Map<String, String> capturedMethod = new LinkedHashMap<>();
            capturedMethod.put("__method__", signature.toShortString());
            for (int i = 0; i < paramNames.length; i++) {
                capturedMethod.put(paramNames[i], ObjectUtils.nullSafeToString(paramArgs[i]));
            }

            capturedMethods.add(capturedMethod);
            attributes.getRequest().setAttribute(CAPTURED_ARGS_KEY, capturedMethods);
        } catch (Exception e) {
            LOGGER.warn("Failed to capture method arguments on exception", e);
        }
    }
}
