package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.common.util.TsidGenerator;
import com.yosmerry.pims.user.entity.User;
import com.yosmerry.pims.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
}
