package com.yosmerry.pims.auth.validation;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if (request == null || Objects.equals(request.getPassword(), request.getConfirmPassword())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("PasswordMismatch")
                .addPropertyNode("confirmPassword")
                .addConstraintViolation();
        return false;
    }
}
