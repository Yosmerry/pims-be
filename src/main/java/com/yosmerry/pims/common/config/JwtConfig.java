package com.yosmerry.pims.common.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class JwtConfig {

  private static final String ISSUER = "pims-be";

  @Bean
  public JwtEncoder jwtEncoder(AuthProperties authProperties) {
    SecretKey secretKey = createSecretKey(authProperties);
    ImmutableSecret<SecurityContext> secret = new ImmutableSecret<>(secretKey);
    return new NimbusJwtEncoder(secret);
  }

  @Bean
  public JwtDecoder jwtDecoder(AuthProperties authProperties) {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
        .withSecretKey(createSecretKey(authProperties))
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
    jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
    return jwtDecoder;
  }

  private SecretKey createSecretKey(AuthProperties authProperties) {
    return new SecretKeySpec(
        authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8),
        "HmacSHA256");
  }
}
