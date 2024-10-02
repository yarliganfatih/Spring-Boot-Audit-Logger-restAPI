package com.draft.restapi.common.payload;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolation;

import com.draft.restapi.common.enums.ConstraintPattern;
import com.draft.restapi.common.exception.DataTruncationException;
import com.draft.restapi.common.exception.ForeignKeyException;
import com.draft.restapi.common.exception.NotNullableException;
import com.draft.restapi.common.helper.RegexHelper;

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

    public ValidationError(DuplicateKeyException duplicateKeyEx) {
        this.setFieldByEx(duplicateKeyEx);
        this.setRejectedValueByEx(duplicateKeyEx);
        this.setCode("duplicate");
        this.setMessage("Value already exists");
    }

    public ValidationError(ForeignKeyException foreignKeyEx) {
        this.setFieldByEx(foreignKeyEx);
        this.setCode("referenceViolation");
        this.setMessage("Cannot be performed because of reference");
    }

    public ValidationError(NotNullableException notNullableEx) {
        this.setFieldByEx(notNullableEx);
        this.setCode("notNullable");
        this.setMessage("Missing required field");
    }

    public ValidationError(DataTruncationException truncationEx) {
        this.setFieldByEx(truncationEx);
        this.setCode("dataTruncation");
        this.setMessage("Cannot exceed maximum length");
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

    public void setFieldByEx(DuplicateKeyException duplicateKeyEx) {
        org.hibernate.exception.ConstraintViolationException cvEx = (org.hibernate.exception.ConstraintViolationException) duplicateKeyEx.getCause();
        this.field = RegexHelper.extractKey(cvEx.getConstraintName(), ConstraintPattern.UNIQUE_KEY.getRegexPattern(), 2);
    }

    public void setFieldByEx(ForeignKeyException foreignKeyEx) {
        org.hibernate.exception.ConstraintViolationException cvEx = (org.hibernate.exception.ConstraintViolationException) foreignKeyEx.getCause();
        String constraintName = cvEx.getConstraintName();
        if (constraintName == null) { // Fallback to parsing the message if constraintName is not available
            constraintName = RegexHelper.extractKey(foreignKeyEx.getMessage(), "CONSTRAINT `(.+?)` FOREIGN KEY", 1);
        }
        String tableName = RegexHelper.extractKey(constraintName, ConstraintPattern.FOREIGN_KEY.getRegexPattern(), 1);
        String fieldName = RegexHelper.extractKey(constraintName, ConstraintPattern.FOREIGN_KEY.getRegexPattern(), 2);
        this.field = tableName + "." + fieldName;
    }

    public void setFieldByEx(NotNullableException notNullableEx) {
        String dbErrorMessage = notNullableEx.getMostSpecificCause().getMessage();
        this.field = RegexHelper.extractKey(dbErrorMessage, "Column '(.*?)' cannot be null", 1); // for MySQL
        if (this.field == null) { // Fallback to another pattern if the first one doesn't match
            this.field = RegexHelper.extractKey(dbErrorMessage, "NULL not allowed for column \"(.*?)\"", 1); // for Hibernate
        }
    }

    public void setFieldByEx(DataTruncationException truncationEx) {
        String dbErrorMessage = truncationEx.getMostSpecificCause().getMessage();
        this.field = RegexHelper.extractKey(dbErrorMessage, "Data too long for column '(.*?)' at row", 1); // for MySQL
        if (this.field == null) { // Fallback to another pattern if the first one doesn't match
            this.field = RegexHelper.extractKey(dbErrorMessage, "Value too long for column \"(.*?) ", 1); // for Hibernate
        }
    }

    public void setRejectedValueByEx(DuplicateKeyException duplicateKeyEx) {
        String dbErrorMessage = duplicateKeyEx.getMostSpecificCause().getMessage();
        this.rejectedValue = RegexHelper.extractKey(dbErrorMessage, "Duplicate entry '(.*?)' for key", 1);
    }
}
