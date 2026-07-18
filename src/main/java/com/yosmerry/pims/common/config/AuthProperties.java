package com.yosmerry.pims.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pims.auth")
public record AuthProperties(
    long accessTokenExpiresIn,
    long refreshTokenExpiresIn,
    String jwtSecret,
    boolean cookieSecure) {
}
