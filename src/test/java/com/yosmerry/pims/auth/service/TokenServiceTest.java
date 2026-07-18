package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.entity.RefreshToken;
import com.yosmerry.pims.auth.repository.RefreshTokenRepository;
import com.yosmerry.pims.common.config.AuthProperties;
import com.yosmerry.pims.common.util.CodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock
  private JwtEncoder jwtEncoder;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @Mock
  private CodeGenerator codeGenerator;

  @Mock
  private AuthProperties authProperties;

  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService = new TokenService(
        jwtEncoder,
        refreshTokenRepository,
        codeGenerator,
        authProperties);
  }

  @Test
  void shouldRevokeRefreshTokenOwnedByUser() {
    RefreshToken refreshToken = refreshToken("USR000001");
    when(refreshTokenRepository.findByTokenHashAndMarkForDeleteFalse(anyString()))
        .thenReturn(Optional.of(refreshToken));

    boolean revoked = tokenService.revokeRefreshToken(
        "refresh-token",
        "USR000001",
        123456789L);

    assertThat(revoked).isTrue();
    assertThat(refreshToken.getRevokedDate()).isEqualTo(123456789L);
    assertThat(refreshToken.getUpdatedBy()).isEqualTo("USR000001");
    verify(refreshTokenRepository).save(refreshToken);
  }

  @Test
  void shouldNotRevokeRefreshTokenOwnedByAnotherUser() {
    RefreshToken refreshToken = refreshToken("USR000002");
    when(refreshTokenRepository.findByTokenHashAndMarkForDeleteFalse(anyString()))
        .thenReturn(Optional.of(refreshToken));

    boolean revoked = tokenService.revokeRefreshToken(
        "refresh-token",
        "USR000001",
        123456789L);

    assertThat(revoked).isFalse();
    assertThat(refreshToken.getRevokedDate()).isNull();
    verify(refreshTokenRepository, never()).save(refreshToken);
  }

  private RefreshToken refreshToken(String userCode) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUserCode(userCode);
    return refreshToken;
  }
}
