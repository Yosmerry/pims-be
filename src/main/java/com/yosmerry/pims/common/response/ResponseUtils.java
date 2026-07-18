package com.yosmerry.pims.common.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseUtils {

  public static <T> ResponseEntity<ApiResponse<T>> created(
      T data,
      String requestId) {
    ApiResponse<T> response = new ApiResponse<>(
        data,
        new Metadata(requestId));

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  public static <T> ResponseEntity<ApiResponse<T>> ok(
      T data,
      String requestId) {
    ApiResponse<T> response = new ApiResponse<>(
        data,
        new Metadata(requestId));

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }
}