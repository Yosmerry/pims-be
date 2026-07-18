package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.entity.RefreshToken;
import com.yosmerry.pims.auth.model.IssuedTokens;
import com.yosmerry.pims.auth.repository.RefreshTokenRepository;
import com.yosmerry.pims.common.config.AuthProperties;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final JwtEncoder jwtEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final CodeGenerator codeGenerator;
  private final AuthProperties authProperties;

  public IssuedTokens issue(User user, long currentTime) {
    String accessToken = createAccessToken(user);
    String refreshTokenValue = createRefreshTokenValue();

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setCode(codeGenerator.next(CodeType.REFRESH_TOKEN));
    refreshToken.setUserCode(user.getCode());
    refreshToken.setTokenHash(hash(refreshTokenValue));
    refreshToken.setExpiresDate(
        currentTime + authProperties.refreshTokenExpiresIn() * 1000);
    refreshToken.setCreatedBy(user.getEmail());
    refreshToken.setUpdatedBy(user.getEmail());
    refreshTokenRepository.save(refreshToken);

    return IssuedTokens.builder()
        .accessToken(accessToken)
        .accessTokenExpiresIn(authProperties.accessTokenExpiresIn())
        .refreshToken(refreshTokenValue)
        .refreshTokenExpiresIn(authProperties.refreshTokenExpiresIn())
        .build();
  }

  private String createAccessToken(User user) {
    Instant issuedAt = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("pims-be")
        .subject(user.getCode())
        .issuedAt(issuedAt)
        .expiresAt(issuedAt.plusSeconds(authProperties.accessTokenExpiresIn()))
        .claim("email", user.getEmail())
        .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

    return jwtEncoder
        .encode(JwtEncoderParameters.from(header, claims))
        .getTokenValue();
  }

  private String createRefreshTokenValue() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashedValue = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashedValue);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}
