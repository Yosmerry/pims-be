package com.yosmerry.pims.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserResponse user) {

  @Builder
  public record UserResponse(
      String code,
      String name,
      String email,
      String status) {
  }
}
