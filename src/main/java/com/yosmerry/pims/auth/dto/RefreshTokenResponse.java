package com.yosmerry.pims.auth.dto;

import lombok.Builder;

@Builder
public record RefreshTokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn) {
}
