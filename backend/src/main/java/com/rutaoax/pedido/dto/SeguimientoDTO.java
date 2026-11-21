package com.rutaoax.pedido.dto;

import com.rutaoax.pedido.EstadoPedido;
import java.time.LocalDateTime;
import java.util.List;

/** Lo que ve el cliente de su pedido: estado, quién lo lleva, cuántas paradas faltan antes de la suya y la línea de tiempo. */
public record SeguimientoDTO(Long pedidoId, EstadoPedido estado, NegocioRefDTO negocio, RepartidorSeguimiento repartidor,
                             int paradasAntes, List<Paso> pasos) {

    /** tipo: EMPRESA o INDEPENDIENTE. */
    public record RepartidorSeguimiento(String nombre, String tipo) {
    }

    /** clave: RECIBIDO, ASIGNADO, EN_CAMINO, ENTREGADO o NO_ENTREGADO. */
    public record Paso(String clave, boolean hecho, LocalDateTime fecha) {
    }
}
