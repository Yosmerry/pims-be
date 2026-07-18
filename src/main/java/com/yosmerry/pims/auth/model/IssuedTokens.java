package com.yosmerry.pims.auth.model;

import lombok.Builder;

@Builder
public record IssuedTokens(
    String accessToken,
    long accessTokenExpiresIn,
    String refreshToken,
    long refreshTokenExpiresIn) {
}
