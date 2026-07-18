package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.dto.LoginRequest;
import com.yosmerry.pims.auth.dto.LoginResponse;
import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.auth.model.IssuedTokens;
import com.yosmerry.pims.auth.model.LoginResult;
import com.yosmerry.pims.common.constant.ErrorCodes;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.exception.ApiAuthenticationException;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.common.util.TsidGenerator;
import com.yosmerry.pims.user.entity.User;
import com.yosmerry.pims.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CodeGenerator codeGenerator;
  private final TokenService tokenService;

  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

    long currentTime = System.currentTimeMillis();
    User user = new User();
    user.setId(TsidGenerator.next());
    user.setCode(codeGenerator.next(CodeType.USER));
    user.setName(request.getName().trim());
    user.setEmail(normalizedEmail);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setStatus(ActiveStatus.ACTIVE);
    user.setCreatedDate(currentTime);
    user.setCreatedBy(normalizedEmail);
    user.setUpdatedDate(currentTime);
    user.setUpdatedBy(normalizedEmail);

    User savedUser = userRepository.save(user);
    return new RegisterResponse(
        savedUser.getCode(),
        savedUser.getName(),
        savedUser.getEmail(),
        savedUser.getStatus().name(),
        savedUser.getCreatedDate(),
        savedUser.getUpdatedDate());
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
    user.setUpdatedDate(currentTime);
    user.setUpdatedBy(user.getEmail());
    userRepository.save(user);

    IssuedTokens tokens = tokenService.issue(user, currentTime);
    LoginResponse response = new LoginResponse(
        tokens.accessToken(),
        "Bearer",
        tokens.accessTokenExpiresIn(),
        new LoginResponse.UserResponse(
            user.getCode(),
            user.getName(),
            user.getEmail(),
            user.getStatus().name()));

    return new LoginResult(
        response,
        tokens.refreshToken(),
        tokens.refreshTokenExpiresIn());
  }
}
