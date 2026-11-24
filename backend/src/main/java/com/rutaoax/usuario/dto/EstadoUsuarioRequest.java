package com.rutaoax.usuario.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoUsuarioRequest(@NotNull(message = "Indica si la cuenta queda activa") Boolean activo) {
}
