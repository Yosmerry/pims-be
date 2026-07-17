package com.yosmerry.pims.auth.dto;

import com.yosmerry.pims.auth.validation.PasswordsMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordsMatch
public class RegisterRequest {

    @NotBlank(message = "Blank")
    @Size(max = 150, message = "CharacterMoreThan150")
    private String name;

    @NotBlank(message = "Blank")
    @Email(message = "InvalidFormat")
    @Size(max = 255, message = "CharacterMoreThan255")
    private String email;

    @NotBlank(message = "Blank")
    @Size(min = 8, message = "CharacterLessThan8")
    @Size(max = 72, message = "CharacterMoreThan72")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "WeakPassword"
    )
    private String password;

    @NotBlank(message = "Blank")
    private String confirmPassword;
}
