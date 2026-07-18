package com.yosmerry.pims.auth.controller;

import com.yosmerry.pims.auth.dto.LoginRequest;
import com.yosmerry.pims.auth.dto.LoginResponse;
import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.auth.dto.RefreshTokenResponse;
import com.yosmerry.pims.auth.model.LoginResult;
import com.yosmerry.pims.auth.service.AuthService;
import com.yosmerry.pims.common.config.AuthProperties;
import com.yosmerry.pims.common.constant.BasePathNames;
import com.yosmerry.pims.common.request.RequestHeaders;
import com.yosmerry.pims.common.response.ApiResponse;
import com.yosmerry.pims.common.response.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(BasePathNames.AUTH)
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

  private final AuthService authService;
  private final AuthProperties authProperties;

  @PostMapping("/register")
  @Operation(description = "Create a new user account")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @Valid @RequestBody RegisterRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    RegisterResponse response = authService.register(request);

    return ResponseUtils.created(
        response,
        requestHeaders.requestId());
  }

  @PostMapping("/login")
  @Operation(description = "Authenticate user and refresh tokens")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @Valid @RequestBody LoginRequest request) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    LoginResult result = authService.login(request);
    ResponseCookie refreshTokenCookie = createRefreshTokenCookie(result);

    return ResponseUtils.ok(
        result.response(),
        requestHeaders.requestId(),
        refreshTokenCookie);
  }

  @PostMapping("/refresh")
  @Operation(description = "Issue a new access token using a refresh token")
  public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
      @Parameter(hidden = true) @RequestHeader HttpHeaders httpHeaders,
      @CookieValue(name = "refresh_token", required = false) String refreshToken) {
    RequestHeaders requestHeaders = RequestHeaders.from(httpHeaders);
    RefreshTokenResponse response = authService.refresh(refreshToken);

    return ResponseUtils.ok(
        response,
        requestHeaders.requestId());
  }

  private ResponseCookie createRefreshTokenCookie(LoginResult result) {
    return ResponseCookie.from("refresh_token", result.refreshToken())
        .httpOnly(true)
        .secure(authProperties.cookieSecure())
        .sameSite("Strict")
        .path(BasePathNames.AUTH)
        .maxAge(result.refreshTokenExpiresIn())
        .build();
  }
}
