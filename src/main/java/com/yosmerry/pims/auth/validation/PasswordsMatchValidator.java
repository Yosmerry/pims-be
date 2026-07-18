package com.yosmerry.pims.auth.validation;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class PasswordsMatchValidator
    implements ConstraintValidator<PasswordsMatch, RegisterRequest> {

  private String path;

  @Override
  public void initialize(PasswordsMatch constraintAnnotation) {
    path = constraintAnnotation.path();
  }

  @Override
  public boolean isValid(
      RegisterRequest request,
      ConstraintValidatorContext context) {
    if (request == null) {
      return true;
    }

    String password = request.getPassword();
    String confirmPassword = request.getConfirmPassword();

    if (isBlank(password) || isBlank(confirmPassword)) {
      return true;
    }

    if (Objects.equals(password, confirmPassword)) {
      return true;
    }

    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(
        context.getDefaultConstraintMessageTemplate())
        .addPropertyNode(path)
        .addConstraintViolation();

    return false;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}