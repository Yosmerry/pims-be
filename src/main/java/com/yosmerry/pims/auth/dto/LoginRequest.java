package com.yosmerry.pims.auth.dto;

import com.yosmerry.pims.common.constant.ErrorCodes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

  @NotBlank(message = ErrorCodes.BLANK)
  @Email(message = ErrorCodes.INVALID_FORMAT)
  private String email;

  @NotBlank(message = ErrorCodes.BLANK)
  private String password;
}
