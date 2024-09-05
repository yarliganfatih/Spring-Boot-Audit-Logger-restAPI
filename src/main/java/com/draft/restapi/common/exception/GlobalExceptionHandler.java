package com.draft.restapi.common.exception;

import com.draft.restapi.common.payload.ApiResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error("Access Denied, You don't have permission to access this resource");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        String errorMessage = ErrorMessageConstants.EXCEPTION_MESSAGES.getOrDefault(
                ex.getClass().getSimpleName(),
                "Invalid request, Please check your input and try again.");
        ApiResponse<Object> response = ApiResponse.error(errorMessage);
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class) // rest of unhandled exceptions
    public ResponseEntity<ApiResponse<Object>> handleExceptions(Exception ex, WebRequest request) {
        LOGGER.error("An unexpected error occurred: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred, Please try again later");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
