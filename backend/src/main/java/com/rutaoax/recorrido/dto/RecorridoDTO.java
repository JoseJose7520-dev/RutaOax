package com.rutaoax.recorrido.dto;

import com.rutaoax.pedido.dto.PedidoDTO;
import com.rutaoax.pedido.dto.RepartidorRefDTO;
import com.rutaoax.recorrido.EstadoRecorrido;
import java.time.LocalDate;
import java.util.List;

public record RecorridoDTO(Long id, LocalDate fecha, EstadoRecorrido estado, RepartidorRefDTO repartidor,
                           VehiculoRefDTO vehiculo, long total, long entregados, List<PedidoDTO> paradas) {
}
