package com.draft.restapi.common.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class DataTruncationException extends DataIntegrityViolationException {
   public DataTruncationException(String msg) {
      super(msg);
   }

   public DataTruncationException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
