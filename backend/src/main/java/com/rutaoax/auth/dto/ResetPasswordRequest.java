package com.rutaoax.auth.dto;

import com.rutaoax.common.validation.PasswordSegura;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "El enlace de recuperación no es válido") String token,
        @NotBlank(message = "La contraseña es obligatoria") @PasswordSegura String password) {
}
