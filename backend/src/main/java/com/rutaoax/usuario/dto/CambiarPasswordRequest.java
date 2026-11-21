package com.rutaoax.usuario.dto;

import com.rutaoax.common.validation.PasswordSegura;
import jakarta.validation.constraints.NotBlank;

public record CambiarPasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria") String actual,
        @NotBlank(message = "La contraseña nueva es obligatoria") @PasswordSegura String nueva) {
}
