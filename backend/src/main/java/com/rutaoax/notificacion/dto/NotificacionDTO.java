package com.rutaoax.notificacion.dto;

import com.rutaoax.notificacion.Canal;
import com.rutaoax.notificacion.EstadoEnvio;
import com.rutaoax.notificacion.NotificacionLog;
import java.time.LocalDateTime;

public record NotificacionDTO(Long id, Canal canal, String destino, String evento, EstadoEnvio estado,
                              String detalle, LocalDateTime creadoEn) {

    public static NotificacionDTO from(NotificacionLog n) {
        return new NotificacionDTO(n.getId(), n.getCanal(), n.getDestino(), n.getEvento(), n.getEstado(),
                n.getDetalle(), n.getCreadoEn());
    }
}
