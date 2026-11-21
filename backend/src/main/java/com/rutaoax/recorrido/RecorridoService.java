package com.rutaoax.recorrido;

import com.rutaoax.common.NotFoundException;
import com.rutaoax.pedido.EstadoPedido;
import com.rutaoax.pedido.Pedido;
import com.rutaoax.pedido.PedidoRepository;
import com.rutaoax.pedido.dto.PedidoDTO;
import com.rutaoax.pedido.dto.RepartidorRefDTO;
import com.rutaoax.recorrido.dto.RecorridoDTO;
import com.rutaoax.recorrido.dto.VehiculoRefDTO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecorridoService {

    private final RecorridoRepository recorridos;
    private final PedidoRepository pedidos;

    public RecorridoService(RecorridoRepository recorridos, PedidoRepository pedidos) {
        this.recorridos = recorridos;
        this.pedidos = pedidos;
    }

    @Transactional(readOnly = true)
    public RecorridoDTO aDTO(Long recorridoId) {
        Recorrido r = recorridos.detalle(recorridoId).orElseThrow(() -> new NotFoundException("El recorrido no existe"));
        List<PedidoDTO> paradas = pedidos.paradasDe(recorridoId).stream().map(PedidoDTO::from).toList();
        long entregados = paradas.stream().filter(p -> p.estado() == EstadoPedido.ENTREGADO).count();
        VehiculoRefDTO vehiculo = r.getVehiculo() == null ? null
                : new VehiculoRefDTO(r.getVehiculo().getId(), r.getVehiculo().getTipo(), r.getVehiculo().getPlaca());
        return new RecorridoDTO(r.getId(), r.getFecha(), r.getEstado(),
                new RepartidorRefDTO(r.getRepartidor().getId(), r.getRepartidor().getUsuario().getNombre()),
                vehiculo, paradas.size(), entregados, paradas);
    }

    /** Renumera las paradas 1..n; si el recorrido planeado quedó vacío lo elimina (libera repartidor y vehículo). */
    @Transactional
    public void compactar(Recorrido recorrido) {
        List<Pedido> paradas = pedidos.findByRecorridoIdOrderByOrdenParada(recorrido.getId());
        if (paradas.isEmpty()) {
            if (recorrido.getEstado() == EstadoRecorrido.PLANEADO) {
                recorridos.delete(recorrido);
            }
            return;
        }
        int orden = 1;
        for (Pedido p : paradas) {
            p.setOrdenParada(orden++);
        }
        pedidos.saveAll(paradas);
    }
}
