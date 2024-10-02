package com.draft.restapi.common.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class NotNullableException extends DataIntegrityViolationException {
   public NotNullableException(String msg) {
      super(msg);
   }

   public NotNullableException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
