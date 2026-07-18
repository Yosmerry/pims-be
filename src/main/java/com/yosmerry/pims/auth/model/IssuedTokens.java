package com.yosmerry.pims.auth.model;

public record IssuedTokens(
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    long refreshTokenExpiresIn) {
}
