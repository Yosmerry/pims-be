package com.yosmerry.pims.auth.controller;

import com.yosmerry.pims.auth.dto.RegisterRequest;
import com.yosmerry.pims.auth.dto.RegisterResponse;
import com.yosmerry.pims.auth.service.AuthService;
import com.yosmerry.pims.common.exception.ApiValidationException;
import com.yosmerry.pims.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @RequestHeader("X-CHANNEL-ID") String channelId,
            @RequestHeader("X-SERVICE-ID") String serviceId,
            @RequestHeader(value = "X-REQUEST-ID", required = false) String requestId,
            @Valid @RequestBody RegisterRequest request
    ) {
        validateHeader("channelId", channelId);
        validateHeader("serviceId", serviceId);

        String resolvedRequestId = requestId;
        if (resolvedRequestId == null || resolvedRequestId.isBlank()) {
            resolvedRequestId = UUID.randomUUID().toString();
        }

        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, resolvedRequestId));
    }

    private void validateHeader(String field, String value) {
        if (value.isBlank()) {
            throw new ApiValidationException(Map.of(field, List.of("Blank")));
        }
    }
}
