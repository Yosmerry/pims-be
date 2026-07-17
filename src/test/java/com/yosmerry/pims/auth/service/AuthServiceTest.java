package com.yosmerry.pims.auth.service;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.common.exception.ApiValidationException;
import com.yosmerry.pims.user.entity.User;
import com.yosmerry.pims.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = validRequest();
        when(userRepository.existsByEmailIgnoreCaseAndMarkForDeleteFalse("yos@example.com")).thenReturn(false);
        when(userRepository.nextCode()).thenReturn("USR000001");
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.register(request);

        assertThat(response.code()).isEqualTo("USR000001");
        assertThat(response.email()).isEqualTo("yos@example.com");
        assertThat(response.status()).isEqualTo("ACTIVE");
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        RegisterRequest request = validRequest();
        request.setEmail("YOS@EXAMPLE.COM");
        when(userRepository.existsByEmailIgnoreCaseAndMarkForDeleteFalse("yos@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApiValidationException.class)
                .satisfies(exception -> assertThat(((ApiValidationException) exception).getErrors())
                        .containsEntry("email", java.util.List.of("Duplicate")));
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
