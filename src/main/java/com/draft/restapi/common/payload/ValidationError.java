package com.draft.restapi.common.payload;

import org.springframework.validation.FieldError;

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
    }

    public ValidationError(ConstraintViolation<?> violation) {
        this.field = violation.getPropertyPath().toString();
        this.rejectedValue = violation.getInvalidValue();
        this.code = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        this.message = violation.getMessage();
    }
}
