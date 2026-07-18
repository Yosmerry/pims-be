package com.yosmerry.pims.auth.validation;

import com.yosmerry.pims.user.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@RequiredArgsConstructor
public class UniqueEmailValidator
    implements ConstraintValidator<UniqueEmail, String> {

  private final UserRepository userRepository;

  @Override
  public boolean isValid(
      String email,
      ConstraintValidatorContext context) {
    if (email == null || email.isBlank()) {
      return true;
    }

    String normalizedEmail = email
        .trim()
        .toLowerCase(Locale.ROOT);

    return !userRepository.existsByEmailIgnoreCaseAndMarkForDeleteFalse(
        normalizedEmail);
  }
}