package com.yosmerry.pims.auth.security;

import com.yosmerry.pims.common.constant.ErrorCodes;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.exception.ApiAuthenticationException;
import com.yosmerry.pims.user.entity.User;
import com.yosmerry.pims.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

  private final UserRepository userRepository;

  @Override
  public User requireActiveUser() {
    Authentication authentication = SecurityContextHolder.getContext()
        .getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw unauthorized();
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Jwt jwt)) {
      throw unauthorized();
    }

    String userCode = jwt.getSubject();
    if (userCode == null || userCode.isBlank()) {
      throw unauthorized();
    }

    User user = userRepository
        .findByCodeAndMarkForDeleteFalse(userCode)
        .orElseThrow(this::unauthorized);
    if (user.getStatus() != ActiveStatus.ACTIVE) {
      throw new ApiAuthenticationException(
          HttpStatus.FORBIDDEN,
          ErrorCodes.USER_INACTIVE);
    }

    return user;
  }

  private ApiAuthenticationException unauthorized() {
    return new ApiAuthenticationException(HttpStatus.UNAUTHORIZED);
  }
}
