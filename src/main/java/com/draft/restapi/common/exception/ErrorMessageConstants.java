package com.draft.restapi.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ErrorMessageConstants {

    private ErrorMessageConstants() {
        // Prevent instantiation
    }

    public static final Map<String, String> EXCEPTION_MESSAGES;

    static {
        Map<String, String> map = new HashMap<>();

        // Validation & Binding Errors
        map.put("MethodArgumentNotValidException", "Validation failed for the request. Please check the provided data.");
        map.put("BindException", "Validation failed for the request. Please check the provided data.");

        // Message Read Errors
        map.put("HttpMessageNotReadableException", "Malformed JSON request or invalid data format.");

        // Media Type Errors
        map.put("HttpMediaTypeNotSupportedException", "Unsupported media type. Please check the 'Content-Type' header.");
        map.put("HttpMediaTypeNotAcceptableException", "The requested media type is not supported by the server.");

        // Request Parameter Errors
        map.put("MissingServletRequestParameterException", "A required request parameter or path variable is missing.");
        map.put("MissingPathVariableException", "A required request parameter or path variable is missing.");
        map.put("MissingServletRequestPartException", "A required request parameter or path variable is missing.");
        map.put("TypeMismatchException", "Invalid parameter type provided in the request.");
        map.put("MethodArgumentTypeMismatchException", "Invalid parameter type provided in the request.");

        // Endpoint Not Found
        map.put("NoHandlerFoundException", "The requested endpoint could not be found.");

        // Method Not Supported
        map.put("HttpRequestMethodNotSupportedException", "The specified HTTP method is not supported for this endpoint.");

        EXCEPTION_MESSAGES = Collections.unmodifiableMap(map);
    }
}
