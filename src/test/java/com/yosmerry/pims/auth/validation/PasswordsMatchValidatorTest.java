package com.yosmerry.pims.auth.validation;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordsMatchValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldReturnPasswordMismatchWhenConfirmationIsDifferent() {
        RegisterRequest request = validRequest();
        request.setConfirmPassword("Different123!");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("confirmPassword");
            assertThat(violation.getMessage()).isEqualTo("PasswordMismatch");
        });
    }

    @Test
    void shouldAcceptMatchingPasswords() {
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest());

        assertThat(violations).isEmpty();
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Yos Merry");
        request.setEmail("yos@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");
        return request;
    }
}
