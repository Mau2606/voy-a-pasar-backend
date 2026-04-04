package com.manualjudicial.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @NotBlank(message = "Debes ingresar tu contraseña actual.")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    private String newPassword;
}
