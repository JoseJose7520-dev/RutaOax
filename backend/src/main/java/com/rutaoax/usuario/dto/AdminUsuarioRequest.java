package com.rutaoax.usuario.dto;

import com.rutaoax.common.validation.PasswordSegura;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos con los que el administrador crea una cuenta de cualquier tipo. */
public record AdminUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo es demasiado largo") String email,
        @NotBlank(message = "La contraseña es obligatoria") @PasswordSegura String password,
        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono debe tener 10 dígitos") String telefono,
        @NotBlank(message = "Elige el tipo de cuenta")
        @Pattern(regexp = "CLIENTE|NEGOCIO|INDEPENDIENTE|ADMIN", message = "El tipo de cuenta no es válido") String tipo,
        @Size(max = 120, message = "El nombre del negocio es demasiado largo") String nombreNegocio) {

    @AssertTrue(message = "El nombre del negocio es obligatorio")
    public boolean isNombreNegocioValido() {
        return !"NEGOCIO".equals(tipo) || (nombreNegocio != null && !nombreNegocio.isBlank());
    }
}
