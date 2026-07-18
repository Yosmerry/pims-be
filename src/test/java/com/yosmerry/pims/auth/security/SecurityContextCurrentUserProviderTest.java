package com.yosmerry.pims.auth.security;

import com.yosmerry.pims.common.constant.ErrorCodes;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.exception.ApiAuthenticationException;
import com.yosmerry.pims.user.entity.User;
import com.yosmerry.pims.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityContextCurrentUserProviderTest {

  @Mock
  private UserRepository userRepository;

  private SecurityContextCurrentUserProvider currentUserProvider;

  @BeforeEach
  void setUp() {
    currentUserProvider = new SecurityContextCurrentUserProvider(userRepository);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnActiveCurrentUser() {
    setAuthenticatedUser("USR000001");
    User user = user(ActiveStatus.ACTIVE);
    when(userRepository.findByCodeAndMarkForDeleteFalse("USR000001"))
        .thenReturn(Optional.of(user));

    User result = currentUserProvider.requireActiveUser();

    assertThat(result).isSameAs(user);
  }

  @Test
  void shouldRejectMissingAuthentication() {
    Throwable throwable = catchThrowable(
        currentUserProvider::requireActiveUser);

    assertThat(throwable)
        .isInstanceOf(ApiAuthenticationException.class);
    assertThat(((ApiAuthenticationException) throwable).getStatus())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldRejectDeletedCurrentUser() {
    setAuthenticatedUser("USR000001");
    when(userRepository.findByCodeAndMarkForDeleteFalse("USR000001"))
        .thenReturn(Optional.empty());

    Throwable throwable = catchThrowable(
        currentUserProvider::requireActiveUser);

    assertThat(throwable)
        .isInstanceOf(ApiAuthenticationException.class);
    assertThat(((ApiAuthenticationException) throwable).getStatus())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldRejectInactiveCurrentUser() {
    setAuthenticatedUser("USR000001");
    when(userRepository.findByCodeAndMarkForDeleteFalse("USR000001"))
        .thenReturn(Optional.of(user(ActiveStatus.INACTIVE)));

    Throwable throwable = catchThrowable(
        currentUserProvider::requireActiveUser);

    assertThat(throwable)
        .isInstanceOf(ApiAuthenticationException.class)
        .hasMessage(ErrorCodes.USER_INACTIVE);
    assertThat(((ApiAuthenticationException) throwable).getStatus())
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  private void setAuthenticatedUser(String userCode) {
    Instant issuedAt = Instant.now();
    Jwt jwt = Jwt.withTokenValue("access-token")
        .header("alg", "HS256")
        .subject(userCode)
        .issuedAt(issuedAt)
        .expiresAt(issuedAt.plusSeconds(900))
        .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
  }

  private User user(ActiveStatus status) {
    User user = new User();
    user.setCode("USR000001");
    user.setStatus(status);
    return user;
  }
}
