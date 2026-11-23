package com.rutaoax.independiente.dto;

import com.rutaoax.pedido.Pedido;
import com.rutaoax.pedido.dto.NegocioRefDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una entrega abierta vista por el independiente: no incluye el teléfono del cliente hasta que la toma. */
public record DisponibleDTO(Long id, String origen, String direccion, String zona, BigDecimal lat, BigDecimal lng,
                            int articulos, BigDecimal pagoRepartidor, NegocioRefDTO negocio, LocalDateTime creadoEn,
                            double distanciaKm) {

    public static DisponibleDTO from(Pedido p, double distanciaKm) {
        NegocioRefDTO negocio = p.getNegocio() == null ? null
                : new NegocioRefDTO(p.getNegocio().getId(), p.getNegocio().getNombre());
        return new DisponibleDTO(p.getId(), p.getOrigen(), p.getDireccion(), p.getZona(), p.getLat(), p.getLng(),
                p.getArticulos(), p.getPagoRepartidor(), negocio, p.getCreadoEn(), distanciaKm);
    }
}
