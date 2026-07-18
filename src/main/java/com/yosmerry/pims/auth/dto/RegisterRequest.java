package com.yosmerry.pims.auth.dto;

import com.yosmerry.pims.auth.validation.PasswordsMatch;
import com.yosmerry.pims.common.constant.ErrorCodes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordsMatch(path = "confirmPassword", message = ErrorCodes.PASSWORD_MISMATCH)
public class RegisterRequest {

  @NotBlank(message = ErrorCodes.BLANK)
  @Size(max = 150, message = ErrorCodes.CHARACTER_MORE_THAN_150)
  private String name;

  @NotBlank(message = ErrorCodes.BLANK)
  @Email(message = ErrorCodes.INVALID_FORMAT)
  @Size(max = 255, message = ErrorCodes.CHARACTER_MORE_THAN_255)
  private String email;

  @NotBlank(message = ErrorCodes.BLANK)
  @Size(min = 8, message = ErrorCodes.CHARACTER_LESS_THAN_8)
  @Size(max = 72, message = ErrorCodes.CHARACTER_MORE_THAN_72)
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = ErrorCodes.WEAK_PASSWORD)
  private String password;

  @NotBlank(message = ErrorCodes.BLANK)
  private String confirmPassword;
}
