package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.dto.LoginRequest;
import com.yosmerry.pims.auth.dto.LoginResponse;
import com.yosmerry.pims.auth.dto.RefreshTokenRequest;
import com.yosmerry.pims.auth.dto.RefreshTokenResponse;
import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Locale;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CodeGenerator codeGenerator;
  private final TokenService tokenService;

  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

    User user = new User();
    user.setCode(codeGenerator.next(CodeType.USER));
    user.setName(request.getName().trim());
    user.setEmail(normalizedEmail);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setStatus(ActiveStatus.ACTIVE);
    user.setCreatedBy(normalizedEmail);
    user.setUpdatedBy(normalizedEmail);

    User savedUser = userRepository.save(user);
    return RegisterResponse.builder()
        .code(savedUser.getCode())
        .name(savedUser.getName())
        .email(savedUser.getEmail())
        .status(savedUser.getStatus().name())
        .createdDate(savedUser.getCreatedDate())
        .updatedDate(savedUser.getUpdatedDate())
        .build();
  }

  @Transactional
  public LoginResult login(LoginRequest request) {
    String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
    User user = userRepository
        .findByEmailIgnoreCaseAndMarkForDeleteFalse(normalizedEmail)
        .orElseThrow(() -> new ApiAuthenticationException(
            HttpStatus.UNAUTHORIZED,
            ErrorCodes.INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new ApiAuthenticationException(
          HttpStatus.UNAUTHORIZED,
          ErrorCodes.INVALID_CREDENTIALS);
    }

    if (user.getStatus() != ActiveStatus.ACTIVE) {
      throw new ApiAuthenticationException(
          HttpStatus.FORBIDDEN,
          ErrorCodes.USER_INACTIVE);
    }

    long currentTime = System.currentTimeMillis();
    user.setLastLoginDate(currentTime);
    user.setUpdatedBy(user.getEmail());
    userRepository.save(user);

    IssuedTokens tokens = tokenService.issue(user, currentTime);
    LoginResponse.UserResponse userResponse = LoginResponse.UserResponse.builder()
        .code(user.getCode())
        .name(user.getName())
        .email(user.getEmail())
        .status(user.getStatus().name())
        .build();

    LoginResponse response = LoginResponse.builder()
        .accessToken(tokens.accessToken())
        .tokenType("Bearer")
        .expiresIn(tokens.accessTokenExpiresIn())
        .user(userResponse)
        .build();

    return LoginResult.builder()
        .response(response)
        .refreshToken(tokens.refreshToken())
        .refreshTokenExpiresIn(tokens.refreshTokenExpiresIn())
        .build();
  }

  @Transactional(readOnly = true)
  public RefreshTokenResponse refresh(@Valid RefreshTokenRequest request) {
    long currentTime = System.currentTimeMillis();
    RefreshToken refreshToken = tokenService
        .findValidRefreshToken(request.refreshToken(), currentTime)
        .orElseThrow(() -> refreshTokenException(ErrorCodes.INVALID));
    User user = userRepository
        .findByCodeAndMarkForDeleteFalse(refreshToken.getUserCode())
        .filter(currentUser -> currentUser.getStatus() == ActiveStatus.ACTIVE)
        .orElseThrow(() -> refreshTokenException(ErrorCodes.INVALID));

    return RefreshTokenResponse.builder()
        .accessToken(tokenService.generateAccessToken(user))
        .tokenType("Bearer")
        .expiresIn(tokenService.getAccessTokenExpiresIn())
        .build();
  }

  @Transactional
  public void logout(
      @Valid RefreshTokenRequest request,
      String userCode) {
    boolean revoked = tokenService.revokeRefreshToken(
        request.refreshToken(),
        userCode,
        System.currentTimeMillis());
    if (!revoked) {
      throw refreshTokenException(ErrorCodes.INVALID);
    }
  }

  private ApiAuthenticationException refreshTokenException(String errorCode) {
    return new ApiAuthenticationException(
        HttpStatus.UNAUTHORIZED,
        "refreshToken",
        errorCode);
  }
}
