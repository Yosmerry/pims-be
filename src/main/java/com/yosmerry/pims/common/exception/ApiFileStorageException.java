package com.yosmerry.pims.common.exception;

public class ApiFileStorageException extends RuntimeException {

  public ApiFileStorageException(Throwable cause) {
    super("File storage operation failed", cause);
  }
}
