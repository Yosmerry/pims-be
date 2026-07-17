package com.yosmerry.pims.common.exception;

import com.yosmerry.pims.common.response.ApiErrorResponse;
import com.yosmerry.pims.common.response.Metadata;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String REQUEST_ID_HEADER = "X-REQUEST-ID";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.computeIfAbsent(fieldError.getField(), ignored -> new ArrayList<>())
                        .add(fieldError.getDefaultMessage())
        );

        return badRequest(errors, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {
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
            HttpServletRequest request
    ) {
        return badRequest(exception.getErrors(), request);
    }

    private ResponseEntity<ApiErrorResponse> badRequest(
            Map<String, List<String>> errors,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                errors,
                new Metadata(resolveRequestId(request))
        );
        return ResponseEntity.badRequest().body(response);
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }
}
