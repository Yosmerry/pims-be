package com.yosmerry.pims.auth.controller;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.auth.service.AuthService;
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
import org.springframework.http.ResponseEntity;
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
}
