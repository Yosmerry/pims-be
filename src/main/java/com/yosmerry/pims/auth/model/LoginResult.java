package com.yosmerry.pims.auth.model;

import com.yosmerry.pims.auth.dto.LoginResponse;
import lombok.Builder;

@Builder
public record LoginResult(
    LoginResponse response,
    String refreshToken,
    long refreshTokenExpiresIn) {
}
