package com.rutaoax.mensaje.dto;

import com.rutaoax.mensaje.Mensaje;
import java.time.LocalDateTime;

public record MensajeDTO(Long id, Long pedidoId, PersonaDTO remitente, String texto, boolean mio, boolean leido,
                         LocalDateTime creadoEn) {

    public static MensajeDTO from(Mensaje m, Long yo) {
        return new MensajeDTO(m.getId(), m.getPedido().getId(),
                new PersonaDTO(m.getRemitente().getId(), m.getRemitente().getNombre()), m.getTexto(),
                m.getRemitente().getId().equals(yo), m.isLeido(), m.getCreadoEn());
    }
}
