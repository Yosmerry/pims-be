package com.yosmerry.pims.common.exception;

import com.yosmerry.pims.common.constant.ErrorCodes;
import com.yosmerry.pims.common.response.ApiErrorResponse;
import com.yosmerry.pims.common.response.Metadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String REQUEST_ID_HEADER = "X-REQUEST-ID";
  private static final String REFRESH_TOKEN_FIELD = "refreshToken";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpServletRequest request) {
    Map<String, List<String>> errors = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors()
        .forEach(fieldError -> errors.computeIfAbsent(fieldError.getField(), ignored -> new ArrayList<>())
            .add(fieldError.getDefaultMessage()));

    return badRequest(errors, request);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(
      MissingRequestHeaderException exception,
      HttpServletRequest request) {
    String field = switch (exception.getHeaderName()) {
      case "X-CHANNEL-ID" -> "channelId";
      case "X-SERVICE-ID" -> "serviceId";
      default -> exception.getHeaderName();
    };

    return badRequest(Map.of(field, List.of("Blank")), request);
  }

  @ExceptionHandler(ApiValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleApiValidation(
      ApiValidationException exception,
      HttpServletRequest request) {
    return badRequest(exception.getErrors(), request);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodValidation(
      HandlerMethodValidationException exception,
      HttpServletRequest request) {
    Map<String, List<String>> errors = new LinkedHashMap<>();
    for (ParameterValidationResult result : exception.getParameterValidationResults()) {
      String field = resolveValidationField(result);
      for (MessageSourceResolvable error : result.getResolvableErrors()) {
        String message = error.getDefaultMessage();
        if (message == null) {
          message = ErrorCodes.INVALID;
        }
        errors.computeIfAbsent(field, ignored -> new ArrayList<>())
            .add(message);
      }
    }

    if (errors.containsKey(REFRESH_TOKEN_FIELD)) {
      return errorResponse(HttpStatus.UNAUTHORIZED, errors, request);
    }

    return badRequest(errors, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception,
      HttpServletRequest request) {
    Map<String, List<String>> errors = new LinkedHashMap<>();
    for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
      String field = resolveValidationField(violation);
      errors.computeIfAbsent(field, ignored -> new ArrayList<>())
          .add(violation.getMessage());
    }

    if (errors.containsKey(REFRESH_TOKEN_FIELD)) {
      return errorResponse(HttpStatus.UNAUTHORIZED, errors, request);
    }

    return badRequest(errors, request);
  }

  @ExceptionHandler(ApiAuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleApiAuthentication(
      ApiAuthenticationException exception,
      HttpServletRequest request) {
    Map<String, List<String>> errors = new LinkedHashMap<>();
    if (exception.getField() != null && exception.getErrorCode() != null) {
      errors.put(
          exception.getField(),
          List.of(exception.getErrorCode()));
    }
    return errorResponse(exception.getStatus(), errors, request);
  }

  @ExceptionHandler(ApiResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
      ApiResourceNotFoundException exception,
      HttpServletRequest request) {
    Map<String, List<String>> errors = Map.of(
        exception.getField(),
        List.of(exception.getErrorCode()));
    return errorResponse(HttpStatus.NOT_FOUND, errors, request);
  }

  private ResponseEntity<ApiErrorResponse> badRequest(
      Map<String, List<String>> errors,
      HttpServletRequest request) {
    return errorResponse(HttpStatus.BAD_REQUEST, errors, request);
  }

  private ResponseEntity<ApiErrorResponse> errorResponse(
      HttpStatus status,
      Map<String, List<String>> errors,
      HttpServletRequest request) {
    ApiErrorResponse response = new ApiErrorResponse(
        status.value(),
            errors,
        new Metadata(resolveRequestId(request)));
    return ResponseEntity.status(status).body(response);
  }

  private String resolveValidationField(ParameterValidationResult result) {
    CookieValue cookieValue = result.getMethodParameter()
        .getParameterAnnotation(CookieValue.class);
    if (cookieValue != null) {
      String cookieName = cookieValue.name();
      if ("refresh_token".equals(cookieName)) {
        return REFRESH_TOKEN_FIELD;
      }
      return cookieName;
    }

    String parameterName = result.getMethodParameter().getParameterName();
    if (parameterName == null) {
      return "request";
    }
    return parameterName;
  }

  private String resolveValidationField(ConstraintViolation<?> violation) {
    String field = "request";
    for (Path.Node node : violation.getPropertyPath()) {
      if (node.getName() != null) {
        field = node.getName();
      }
    }
    return field;
  }

  private String resolveRequestId(HttpServletRequest request) {
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return requestId;
  }
}
