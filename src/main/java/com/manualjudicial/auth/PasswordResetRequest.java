package com.manualjudicial.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank
    @Email
    private String email;

    /** reCAPTCHA v2 token from the frontend */
    private String captchaToken;
}
