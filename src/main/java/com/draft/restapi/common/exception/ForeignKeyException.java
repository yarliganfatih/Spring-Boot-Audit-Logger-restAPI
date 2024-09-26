package com.draft.restapi.common.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class ForeignKeyException extends DataIntegrityViolationException {
   public ForeignKeyException(String msg) {
      super(msg);
   }

   public ForeignKeyException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
