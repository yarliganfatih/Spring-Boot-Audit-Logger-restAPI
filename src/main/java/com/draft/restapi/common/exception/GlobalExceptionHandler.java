package com.draft.restapi.common.exception;

import com.draft.restapi.common.filter.TraceFilter;
import com.draft.restapi.common.payload.ApiResponse;
import com.draft.restapi.common.payload.ValidationError;
import com.draft.restapi.common.helper.RequestHelper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.validation.ConstraintViolationException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.draft.restapi.audit.entity.ErrorLog;
import com.draft.restapi.audit.repository.ErrorLogRepository;
import com.draft.restapi.auth.entity.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.MDC;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private ErrorLogRepository errorLogRepository;

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

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<ApiResponse<Object>> handleRequestRejectedException(RequestRejectedException ex, WebRequest request, HttpServletRequest servletRequest) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Object>> handlePropertyReferenceException(PropertyReferenceException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Invalid request, Please check your input and try again.");
        response.setValidationErrors(Collections.singletonList(new ValidationError(ex)));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Invalid request, Please check your input and try again.");
        List<ValidationError> validationErrors = ex.getConstraintViolations().stream()
                .map(ValidationError::new).collect(Collectors.toList());
        response.setValidationErrors(validationErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Invalid request, There is data integrity violation.");
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String dbErrorMessage = ex.getMostSpecificCause().getMessage();
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            if (dbErrorMessage.contains("Duplicate") || dbErrorMessage.contains("_unique_")) {
                DuplicateKeyException duplicateKeyEx = new DuplicateKeyException(dbErrorMessage, ex.getCause());
                ValidationError validationError = new ValidationError(duplicateKeyEx);
                response.setValidationErrors(Collections.singletonList(validationError));
                status = HttpStatus.CONFLICT;
            } else if (dbErrorMessage.contains("REFERENCES") || dbErrorMessage.contains("_foreign_")) {
                ForeignKeyException foreignKeyEx = new ForeignKeyException(dbErrorMessage, ex.getCause());
                ValidationError validationError = new ValidationError(foreignKeyEx);
                response.setValidationErrors(Collections.singletonList(validationError));
                status = HttpStatus.CONFLICT;
            } else if (dbErrorMessage.contains("NULL") || dbErrorMessage.contains("null")) {
                NotNullableException notNullableEx = new NotNullableException(dbErrorMessage, ex.getCause());
                ValidationError validationError = new ValidationError(notNullableEx);
                response.setValidationErrors(Collections.singletonList(validationError));
            }
        } else if (ex.getCause() instanceof org.hibernate.exception.DataException) {
            if (dbErrorMessage.contains("truncation") || dbErrorMessage.contains("too long")) {
                DataTruncationException truncationEx = new DataTruncationException(dbErrorMessage, ex.getCause());
                ValidationError validationError = new ValidationError(truncationEx);
                response.setValidationErrors(Collections.singletonList(validationError));
            }
        }
        return new ResponseEntity<>(response, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        String errorMessage = ErrorMessageConstants.EXCEPTION_MESSAGES.getOrDefault(
                ex.getClass().getSimpleName(),
                "Invalid request, Please check your input and try again.");
        ApiResponse<Object> response = ApiResponse.error(errorMessage);
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validationEx = (MethodArgumentNotValidException) ex;
            List<ValidationError> validationErrors = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> new ValidationError(fieldError)).collect(Collectors.toList());
            response.setValidationErrors(validationErrors);
        } else if (ex instanceof BindException) {
            BindException bindEx = (BindException) ex;
            List<ValidationError> validationErrors = bindEx.getBindingResult().getFieldErrors().stream()
                    .map(ValidationError::new).collect(Collectors.toList());
            response.setValidationErrors(validationErrors);
        } else if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException paramEx = (MissingServletRequestParameterException) ex;
            response.setValidationErrors(Collections.singletonList(new ValidationError(paramEx)));
        } else if (ex instanceof MissingServletRequestPartException) {
            MissingServletRequestPartException partEx = (MissingServletRequestPartException) ex;
            response.setValidationErrors(Collections.singletonList(new ValidationError(partEx)));
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException typeEx = (MethodArgumentTypeMismatchException) ex;
            response.setValidationErrors(Collections.singletonList(new ValidationError(typeEx)));
        }
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class) // rest of unhandled exceptions
    public ResponseEntity<ApiResponse<Object>> handleExceptions(Exception ex, WebRequest request, HttpServletRequest servletRequest) {
        LOGGER.error("An unexpected error occurred: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred, Please try again later");
        saveErrorLog(ex, servletRequest, response, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void saveErrorLog(Exception ex, HttpServletRequest servletRequest, Object responseObj, HttpStatus status) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setEndpointUrl(servletRequest.getRequestURI());
            errorLog.setHttpMethod(servletRequest.getMethod());
            errorLog.setErrorMessage(ex.getMessage());
            errorLog.setErrorType(ex.getClass().getName());
            errorLog.setHttpStatusCode(status.value());
            errorLog.setXTraceId(MDC.get(TraceFilter.TRACE_ID));
            errorLog.setOccurredBy(User.getLoggedUser());
            errorLog.setRequestParams(servletRequest.getQueryString());

            try {
                errorLog.setResponseBody(mapper.writeValueAsString(responseObj));
            } catch (Exception ignore) {
                LOGGER.warn("Failed to serialize response body: {}", ignore.getMessage());
                errorLog.setResponseBody("[Unserializable Response]");
            }

            try {
                errorLog.setRequestHeaders(mapper.writeValueAsString(RequestHelper.getRequestHeaders(servletRequest)));
            } catch (Exception ignore) {
                LOGGER.warn("Failed to serialize request headers: {}", ignore.getMessage());
                errorLog.setRequestHeaders("[Unserializable Headers]");
            }

            try {
                errorLog.setRequestBody(RequestHelper.getRequestBody(servletRequest));
            } catch (Exception ignore) {
                LOGGER.warn("Failed to serialize request body: {}", ignore.getMessage());
                errorLog.setRequestBody("[Unsupported Encoding]");
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            errorLog.setErrorStackTrace(sw.toString());

            errorLogRepository.save(errorLog);
        } catch (Exception e) { // do not affect main flow if errorLog saving fails
            LOGGER.error("Failed to save ErrorLog to Database: {}", e.getMessage());
        }
    }
}
