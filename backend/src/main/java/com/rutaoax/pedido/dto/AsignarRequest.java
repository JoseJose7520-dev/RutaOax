package com.rutaoax.pedido.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record AsignarRequest(
        @NotEmpty(message = "Elige al menos un pedido") @Size(max = 100, message = "Máximo 100 pedidos por asignación") List<Long> pedidoIds,
        @NotNull(message = "Elige un repartidor") Long repartidorId,
        @NotNull(message = "Elige un vehículo") Long vehiculoId,
        @NotNull(message = "Elige la fecha")
        @FutureOrPresent(message = "La fecha no puede ser anterior a hoy") LocalDate fecha) {
}
