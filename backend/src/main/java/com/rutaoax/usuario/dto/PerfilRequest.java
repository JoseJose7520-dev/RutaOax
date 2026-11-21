package com.rutaoax.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String nombre,
        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono debe tener 10 dígitos") String telefono,
        @Size(max = 255, message = "La URL del avatar es demasiado larga")
        @Pattern(regexp = "^(https?://\\S+)?$", message = "El avatar debe ser una URL que empiece con http:// o https://") String avatarUrl) {
}
