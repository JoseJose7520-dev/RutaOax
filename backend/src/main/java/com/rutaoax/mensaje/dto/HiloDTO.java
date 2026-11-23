package com.rutaoax.mensaje.dto;

import java.time.LocalDateTime;

/** Una conversación: siempre es sobre un pedido. */
public record HiloDTO(Long pedidoId, String titulo, PersonaDTO contraparte, String ultimoMensaje, LocalDateTime fecha,
                      long noLeidos) {
}
