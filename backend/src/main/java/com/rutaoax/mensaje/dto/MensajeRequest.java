package com.rutaoax.mensaje.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensajeRequest(
        @NotBlank(message = "Escribe un mensaje")
        @Size(max = 1000, message = "El mensaje no puede pasar de 1000 caracteres") String texto) {
}
