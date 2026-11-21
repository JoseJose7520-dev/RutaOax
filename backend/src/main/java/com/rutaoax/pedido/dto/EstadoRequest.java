package com.rutaoax.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EstadoRequest(
        @NotBlank(message = "Indica el nuevo estado")
        @Pattern(regexp = "ENTREGADO|NO_ENTREGADO", message = "El estado debe ser ENTREGADO o NO_ENTREGADO") String estado,
        @Size(max = 200, message = "El motivo es demasiado largo") String motivo) {
}
