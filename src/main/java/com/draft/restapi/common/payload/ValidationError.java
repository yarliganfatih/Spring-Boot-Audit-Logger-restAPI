package com.draft.restapi.common.payload;

import org.springframework.beans.TypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationError {
    private String field;
    private Object rejectedValue;
    private String code;
    private String message;

    public ValidationError(FieldError fieldError) {
        this.field = fieldError.getField();
        this.rejectedValue = fieldError.getRejectedValue();
        this.code = fieldError.getCode();
        this.message = fieldError.getDefaultMessage();
        if (fieldError.contains(TypeMismatchException.class)) {
            customizeErrorMsg(fieldError.unwrap(TypeMismatchException.class));
        }
    }

    public ValidationError(ConstraintViolation<?> violation) {
        this.field = violation.getPropertyPath().toString();
        this.rejectedValue = violation.getInvalidValue();
        this.code = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        this.message = violation.getMessage();
    }

    public ValidationError(MissingServletRequestParameterException paramEx) {
        this.setField(paramEx.getParameterName());
        this.setCode("missingParam");
        this.setMessage("Missing required parameter");
    }

    public ValidationError(MissingServletRequestPartException partEx) {
        this.setField(partEx.getRequestPartName());
        this.setCode("missingPart");
        this.setMessage("Missing required part");
    }

    public ValidationError(MethodArgumentTypeMismatchException typeEx) {
        this.field = typeEx.getName();
        this.rejectedValue = typeEx.getValue();
        this.code = "typeMismatch";
        this.message = typeEx.getMessage();
        customizeErrorMsg(typeEx);
    }

    public void customizeErrorMsg(TypeMismatchException typeEx) {
        String requiredType = typeEx.getRequiredType() != null ? typeEx.getRequiredType().getSimpleName() : "Unknown";
        this.message = "Invalid value for parameter, expected type: " + requiredType;
    }
}
