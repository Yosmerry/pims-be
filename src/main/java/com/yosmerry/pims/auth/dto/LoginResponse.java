package com.yosmerry.pims.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserResponse user) {

  public record UserResponse(
      String code,
      String name,
      String email,
      String status) {
  }
}
