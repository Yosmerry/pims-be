package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.dto.LoginRequest;
import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.auth.dto.RefreshTokenRequest;
import com.yosmerry.pims.auth.dto.RefreshTokenResponse;
import com.yosmerry.pims.auth.entity.RefreshToken;
import com.yosmerry.pims.auth.model.IssuedTokens;
import com.yosmerry.pims.auth.model.LoginResult;
import com.yosmerry.pims.common.constant.ErrorCodes;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.exception.ApiAuthenticationException;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.user.entity.User;
import com.yosmerry.pims.user.repository.UserRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private CodeGenerator codeGenerator;

  @Mock
  private TokenService tokenService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(
        userRepository,
        passwordEncoder,
        codeGenerator,
        tokenService);
  }

  @Test
  void shouldLoginActiveUser() {
    LoginRequest request = loginRequest();
    User user = activeUser();
    when(userRepository.findByEmailIgnoreCaseAndMarkForDeleteFalse("yos@example.com"))
        .thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
    when(tokenService.issue(any(User.class), org.mockito.ArgumentMatchers.anyLong()))
        .thenReturn(IssuedTokens.builder()
            .accessToken("access-token")
            .accessTokenExpiresIn(900)
            .refreshToken("refresh-token")
            .refreshTokenExpiresIn(86400)
            .build());

    LoginResult result = authService.login(request);

    assertThat(result.response().accessToken()).isEqualTo("access-token");
    assertThat(result.response().expiresIn()).isEqualTo(900);
    assertThat(result.refreshToken()).isEqualTo("refresh-token");
    assertThat(user.getLastLoginDate()).isNotNull();
  }

  @Test
  void shouldRejectInvalidCredentials() {
    LoginRequest request = loginRequest();
    when(userRepository.findByEmailIgnoreCaseAndMarkForDeleteFalse("yos@example.com"))
        .thenReturn(java.util.Optional.empty());

    ThrowingCallable action = () -> authService.login(request);

    assertThat(org.assertj.core.api.Assertions.catchThrowable(action))
        .isInstanceOf(ApiAuthenticationException.class)
        .hasMessage(ErrorCodes.INVALID_CREDENTIALS);
  }

  @Test
  void shouldRejectInactiveUser() {
    LoginRequest request = loginRequest();
    User user = activeUser();
    user.setStatus(ActiveStatus.INACTIVE);
    when(userRepository.findByEmailIgnoreCaseAndMarkForDeleteFalse("yos@example.com"))
        .thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

    ThrowingCallable action = () -> authService.login(request);

    assertThat(org.assertj.core.api.Assertions.catchThrowable(action))
        .isInstanceOf(ApiAuthenticationException.class)
        .hasMessage(ErrorCodes.USER_INACTIVE);
  }

  @Test
  void shouldRefreshAccessToken() {
    User user = activeUser();
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUserCode(user.getCode());
    when(tokenService.findValidRefreshToken(
        org.mockito.ArgumentMatchers.eq("refresh-token"),
        org.mockito.ArgumentMatchers.anyLong()))
        .thenReturn(java.util.Optional.of(refreshToken));
    when(userRepository.findByCodeAndMarkForDeleteFalse("USR000001"))
        .thenReturn(java.util.Optional.of(user));
    when(tokenService.generateAccessToken(user)).thenReturn("new-access-token");
    when(tokenService.getAccessTokenExpiresIn()).thenReturn(900L);

    RefreshTokenResponse response = authService.refresh(
        new RefreshTokenRequest("refresh-token"));

    assertThat(response.accessToken()).isEqualTo("new-access-token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresIn()).isEqualTo(900);
  }

  @Test
  void shouldRejectInvalidRefreshToken() {
    when(tokenService.findValidRefreshToken(
        org.mockito.ArgumentMatchers.eq("invalid-token"),
        org.mockito.ArgumentMatchers.anyLong()))
        .thenReturn(java.util.Optional.empty());
    ThrowingCallable action = () -> authService.refresh(
        new RefreshTokenRequest("invalid-token"));

    assertThat(org.assertj.core.api.Assertions.catchThrowable(action))
        .isInstanceOf(ApiAuthenticationException.class)
        .hasMessage(ErrorCodes.INVALID);
  }

  @Test
  void shouldLogoutUser() {
    when(tokenService.revokeRefreshToken(
        org.mockito.ArgumentMatchers.eq("refresh-token"),
        org.mockito.ArgumentMatchers.eq("USR000001"),
        org.mockito.ArgumentMatchers.anyLong()))
        .thenReturn(true);

    authService.logout(
        new RefreshTokenRequest("refresh-token"),
        "USR000001");

    verify(tokenService).revokeRefreshToken(
        org.mockito.ArgumentMatchers.eq("refresh-token"),
        org.mockito.ArgumentMatchers.eq("USR000001"),
        org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void shouldRejectInvalidRefreshTokenOnLogout() {
    when(tokenService.revokeRefreshToken(
        org.mockito.ArgumentMatchers.eq("invalid-token"),
        org.mockito.ArgumentMatchers.eq("USR000001"),
        org.mockito.ArgumentMatchers.anyLong()))
        .thenReturn(false);
    ThrowingCallable action = () -> authService.logout(
        new RefreshTokenRequest("invalid-token"),
        "USR000001");

    assertThat(org.assertj.core.api.Assertions.catchThrowable(action))
        .isInstanceOf(ApiAuthenticationException.class)
        .hasMessage(ErrorCodes.INVALID);
  }

  @Test
  void shouldRegisterUser() {
    RegisterRequest request = validRequest();
    when(codeGenerator.next(CodeType.USER)).thenReturn("USR000001");
    when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RegisterResponse response = authService.register(request);

    assertThat(response.code()).isEqualTo("USR000001");
    assertThat(response.email()).isEqualTo("yos@example.com");
    assertThat(response.status()).isEqualTo("ACTIVE");
    verify(passwordEncoder).encode("Password123!");
  }

  private RegisterRequest validRequest() {
    RegisterRequest request = new RegisterRequest();
    request.setName("Yos Merry");
    request.setEmail("yos@example.com");
    request.setPassword("Password123!");
    request.setConfirmPassword("Password123!");
    return request;
  }

  private LoginRequest loginRequest() {
    LoginRequest request = new LoginRequest();
    request.setEmail("yos@example.com");
    request.setPassword("Password123!");
    return request;
  }

  private User activeUser() {
    User user = new User();
    user.setCode("USR000001");
    user.setName("Yos Merry");
    user.setEmail("yos@example.com");
    user.setPasswordHash("hashed-password");
    user.setStatus(ActiveStatus.ACTIVE);
    return user;
  }
}
