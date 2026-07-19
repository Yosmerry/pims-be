package com.yosmerry.pims.common.exception;

import com.yosmerry.pims.common.constant.ErrorCodes;
import lombok.Getter;

@Getter
public class ApiResourceNotFoundException extends RuntimeException {

  private final String field;
  private final String errorCode;

  public ApiResourceNotFoundException(String field) {
    super(ErrorCodes.NOT_FOUND);
    this.field = field;
    this.errorCode = ErrorCodes.NOT_FOUND;
  }
}
