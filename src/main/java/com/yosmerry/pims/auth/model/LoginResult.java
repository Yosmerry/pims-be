package com.yosmerry.pims.auth.model;

import com.yosmerry.pims.auth.dto.LoginResponse;

public record LoginResult(
    LoginResponse response,
    String refreshToken,
    long refreshTokenExpiresIn) {
}
