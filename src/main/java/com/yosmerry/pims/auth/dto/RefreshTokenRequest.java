package com.yosmerry.pims.auth.dto;

import com.yosmerry.pims.common.constant.ErrorCodes;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
  
    @NotBlank(message = ErrorCodes.MISSING)
    String refreshToken) {
}
