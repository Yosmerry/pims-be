package com.yosmerry.pims.common.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class JwtConfig {

  @Bean
  public JwtEncoder jwtEncoder(AuthProperties authProperties) {
    SecretKey secretKey = new SecretKeySpec(
        authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8),
        "HmacSHA256");
    ImmutableSecret<SecurityContext> secret = new ImmutableSecret<>(secretKey);
    return new NimbusJwtEncoder(secret);
  }
}
