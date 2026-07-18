package com.yosmerry.pims.auth.controller;

import com.yosmerry.pims.auth.security.RefreshTokenCookieFactory;
import com.yosmerry.pims.auth.security.CurrentUserProvider;
import com.yosmerry.pims.auth.service.AuthService;
import com.yosmerry.pims.auth.service.TokenService;
import com.yosmerry.pims.common.exception.GlobalExceptionHandler;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private CodeGenerator codeGenerator;

  @Mock
  private TokenService tokenService;

  @Mock
  private CurrentUserProvider currentUserProvider;

  @Mock
  private RefreshTokenCookieFactory refreshTokenCookieFactory;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    AuthService authServiceTarget = new AuthService(
        userRepository,
        passwordEncoder,
        codeGenerator,
        tokenService,
        currentUserProvider);
    ProxyFactory proxyFactory = new ProxyFactory(authServiceTarget);
    proxyFactory.setProxyTargetClass(true);
    proxyFactory.addAdvice(new MethodValidationInterceptor(
        (jakarta.validation.Validator) validator));
    AuthService authService = (AuthService) proxyFactory.getProxy();

    mockMvc = MockMvcBuilders
        .standaloneSetup(new AuthController(authService, refreshTokenCookieFactory))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .build();
  }

  @Test
  void shouldRejectMissingRefreshTokenCookieBeforeService() throws Exception {
    mockMvc.perform(post("/api/v1/auth/refresh")
            .header("X-CHANNEL-ID", "WEB")
            .header("X-SERVICE-ID", "pims-fe")
            .header("X-REQUEST-ID", "request-id"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(401))
        .andExpect(jsonPath("$.errors.refreshToken[0]").value("Missing"))
        .andExpect(jsonPath("$.metadata.requestId").value("request-id"));

    verifyNoInteractions(
        userRepository,
        passwordEncoder,
        codeGenerator,
        tokenService,
        currentUserProvider);
  }
}
