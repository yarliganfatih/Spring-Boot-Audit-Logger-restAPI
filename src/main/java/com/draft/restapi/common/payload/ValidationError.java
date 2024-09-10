package com.draft.restapi.common.payload;

import org.springframework.validation.FieldError;

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
}
