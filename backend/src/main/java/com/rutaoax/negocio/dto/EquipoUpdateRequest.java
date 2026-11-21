package com.rutaoax.negocio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EquipoUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String nombre,
        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono debe tener 10 dígitos") String telefono) {
}
