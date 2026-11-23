package com.rutaoax.notificacion.dto;

import com.rutaoax.notificacion.EstadoEnvio;

public record PruebaNotificacionDTO(EstadoEnvio estado, String detalle) {
}
