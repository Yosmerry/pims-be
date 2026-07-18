package com.yosmerry.pims.auth.security;

import com.yosmerry.pims.common.config.AuthProperties;
import com.yosmerry.pims.common.constant.BasePathNames;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieFactory {

  private static final String COOKIE_NAME = "refresh_token";
  private static final String SAME_SITE = "Strict";

  private final AuthProperties authProperties;

  public ResponseCookie create(
      String refreshToken,
      long expiresIn) {
    return ResponseCookie.from(COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(authProperties.cookieSecure())
        .sameSite(SAME_SITE)
        .path(BasePathNames.AUTH)
        .maxAge(expiresIn)
        .build();
  }

  public ResponseCookie clear() {
    return ResponseCookie.from(COOKIE_NAME, "")
        .httpOnly(true)
        .secure(authProperties.cookieSecure())
        .sameSite(SAME_SITE)
        .path(BasePathNames.AUTH)
        .maxAge(0)
        .build();
  }
}