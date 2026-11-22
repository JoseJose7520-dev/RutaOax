package com.rutaoax.notificacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PruebaNotificacionRequest(
        @NotBlank(message = "Elige el canal")
        @Pattern(regexp = "EMAIL|SMS|WHATSAPP", message = "El canal debe ser EMAIL, SMS o WHATSAPP") String canal,
        @NotBlank(message = "Escribe el destino (correo o teléfono)")
        @Size(max = 150, message = "El destino es demasiado largo") String destino) {
}
