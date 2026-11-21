package com.rutaoax.pedido.dto;

import com.rutaoax.pedido.EstadoPedido;
import com.rutaoax.pedido.Modalidad;
import com.rutaoax.pedido.Pedido;
import com.rutaoax.recorrido.Recorrido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoDTO(Long id, String clienteNombre, String clienteTelefono, String direccion, String zona,
                        BigDecimal lat, BigDecimal lng, int articulos, BigDecimal total, String notas, String origen,
                        BigDecimal pagoRepartidor, Modalidad modalidad, EstadoPedido estado, NegocioRefDTO negocio,
                        RepartidorRefDTO repartidor, Long recorridoId, Integer ordenParada, LocalDateTime creadoEn) {

    public static PedidoDTO from(Pedido p) {
        NegocioRefDTO negocio = p.getNegocio() == null ? null
                : new NegocioRefDTO(p.getNegocio().getId(), p.getNegocio().getNombre());
        Recorrido rec = p.getRecorrido();
        RepartidorRefDTO repartidor = rec == null ? null
                : new RepartidorRefDTO(rec.getRepartidor().getId(), rec.getRepartidor().getUsuario().getNombre());
        return new PedidoDTO(p.getId(), p.getClienteNombre(), p.getClienteTelefono(), p.getDireccion(), p.getZona(),
                p.getLat(), p.getLng(), p.getArticulos(), p.getTotal(), p.getNotas(), p.getOrigen(),
                p.getPagoRepartidor(), p.getModalidad(), p.getEstado(), negocio, repartidor,
                rec == null ? null : rec.getId(), p.getOrdenParada(), p.getCreadoEn());
    }
}
