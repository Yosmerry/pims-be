package com.yosmerry.pims.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiAuthenticationException extends RuntimeException {

  private final HttpStatus status;
  private final String errorCode;

  public ApiAuthenticationException(HttpStatus status, String errorCode) {
    super(errorCode);
    this.status = status;
    this.errorCode = errorCode;
  }
}
