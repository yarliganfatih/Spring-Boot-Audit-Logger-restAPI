package com.draft.restapi.common.payload;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.Serializable;

import javax.validation.ConstraintViolation;

import com.draft.restapi.common.enums.ConstraintPattern;
import com.draft.restapi.common.exception.DataTruncationException;
import com.draft.restapi.common.exception.ForeignKeyException;
import com.draft.restapi.common.exception.NotNullableException;
import com.draft.restapi.common.helper.RegexHelper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError implements Serializable {
    private static final long serialVersionUID = 1L;

    private String field;
    private Object rejectedValue;
    private String code;
    private String message;

    public ValidationError(FieldError fieldError) {
        this.setField(fieldError.getField());
        this.setRejectedValue(fieldError.getRejectedValue());
        this.setCode(fieldError.getCode());
        this.setMessage(fieldError.getDefaultMessage());
        if (fieldError.contains(TypeMismatchException.class)) {
            customizeErrorMsg(fieldError.unwrap(TypeMismatchException.class));
        }
    }

    public ValidationError(ConstraintViolation<?> violation) {
        this.setField(violation.getPropertyPath().toString());
        this.setRejectedValue(violation.getInvalidValue());
        this.setCode(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
        this.setMessage(violation.getMessage());
    }

    public ValidationError(PropertyReferenceException ex) {
        this.setField("sort");
        this.setRejectedValue(ex.getPropertyName());
        this.setCode("invalidProperty");
        this.setMessage(ex.getMessage());
    }

    public ValidationError(DuplicateKeyException duplicateKeyEx) {
        this.populateFieldFrom(duplicateKeyEx);
        this.populateRejectedValueFrom(duplicateKeyEx);
        this.setCode("duplicate");
        this.setMessage("Value already exists");
    }

    public ValidationError(ForeignKeyException foreignKeyEx) {
        this.populateFieldFrom(foreignKeyEx);
        this.setCode("referenceViolation");
        this.setMessage("Cannot be performed because of reference");
    }

    public ValidationError(NotNullableException notNullableEx) {
        this.populateFieldFrom(notNullableEx);
        this.setCode("notNullable");
        this.setMessage("Missing required field");
    }

    public ValidationError(DataTruncationException truncationEx) {
        this.populateFieldFrom(truncationEx);
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
        this.setField(typeEx.getName());
        this.setRejectedValue(typeEx.getValue());
        this.setCode("typeMismatch");
        this.setMessage(typeEx.getMessage());
        customizeErrorMsg(typeEx);
    }

    private void customizeErrorMsg(TypeMismatchException typeEx) {
        Class<?> requiredTypeClass = typeEx.getRequiredType();
        String requiredType = requiredTypeClass != null ? requiredTypeClass.getSimpleName() : "Unknown";
        this.message = "Invalid value for parameter, expected type: " + requiredType;
    }

    private void populateFieldFrom(DuplicateKeyException duplicateKeyEx) {
        org.hibernate.exception.ConstraintViolationException cvEx = (org.hibernate.exception.ConstraintViolationException) duplicateKeyEx.getCause();
        this.field = RegexHelper.extractKey(cvEx.getConstraintName(), ConstraintPattern.UNIQUE_KEY.getRegexPattern(), 2);
    }

    private void populateFieldFrom(ForeignKeyException foreignKeyEx) {
        org.hibernate.exception.ConstraintViolationException cvEx = (org.hibernate.exception.ConstraintViolationException) foreignKeyEx.getCause();
        String constraintName = cvEx.getConstraintName();
        if (constraintName == null) { // Fallback to parsing the message if constraintName is not available
            constraintName = RegexHelper.extractKey(foreignKeyEx.getMessage(), "CONSTRAINT `(.+?)` FOREIGN KEY", 1);
        }
        String tableName = RegexHelper.extractKey(constraintName, ConstraintPattern.FOREIGN_KEY.getRegexPattern(), 1);
        String fieldName = RegexHelper.extractKey(constraintName, ConstraintPattern.FOREIGN_KEY.getRegexPattern(), 2);
        this.field = tableName + "." + fieldName;
    }

    private void populateFieldFrom(NotNullableException notNullableEx) {
        String dbErrorMessage = notNullableEx.getMostSpecificCause().getMessage();
        this.field = RegexHelper.extractKey(dbErrorMessage, "Column '(.*?)' cannot be null", 1); // for MySQL
        if (this.field == null) { // Fallback to another pattern if the first one doesn't match
            this.field = RegexHelper.extractKey(dbErrorMessage, "NULL not allowed for column \"(.*?)\"", 1); // for Hibernate
        }
    }

    private void populateFieldFrom(DataTruncationException truncationEx) {
        String dbErrorMessage = truncationEx.getMostSpecificCause().getMessage();
        this.field = RegexHelper.extractKey(dbErrorMessage, "Data too long for column '(.*?)' at row", 1); // for MySQL
        if (this.field == null) { // Fallback to another pattern if the first one doesn't match
            this.field = RegexHelper.extractKey(dbErrorMessage, "Value too long for column \"(.*?) ", 1); // for Hibernate
        }
    }

    private void populateRejectedValueFrom(DuplicateKeyException duplicateKeyEx) {
        String dbErrorMessage = duplicateKeyEx.getMostSpecificCause().getMessage();
        this.rejectedValue = RegexHelper.extractKey(dbErrorMessage, "Duplicate entry '(.*?)' for key", 1);
    }
}
