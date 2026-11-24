package com.rutaoax.usuario.dto;

import com.rutaoax.common.validation.PasswordSegura;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Edicion de una cuenta por el administrador. La contraseña solo cambia si se manda. */
public record AdminUsuarioUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo es demasiado largo") String email,
        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono debe tener 10 dígitos") String telefono,
        @PasswordSegura String nuevaPassword) {
}
