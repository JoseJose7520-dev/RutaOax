package com.rutaoax.negocio;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.mensaje.Mensaje;
import com.rutaoax.mensaje.MensajeRepository;
import com.rutaoax.negocio.dto.AtencionDTO;
import com.rutaoax.negocio.dto.RepartidorHoyDTO;
import com.rutaoax.negocio.dto.ResumenDTO;
import com.rutaoax.pedido.EstadoPedido;
import com.rutaoax.pedido.Pedido;
import com.rutaoax.pedido.PedidoRepository;
import com.rutaoax.vehiculo.EtiquetasVehiculo;
import com.rutaoax.vehiculo.Vehiculo;
import com.rutaoax.vehiculo.VehiculoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumenService {

    private static final int MAXIMO_POR_TIPO = 5;

    private final NegocioContexto contexto;
    private final PedidoRepository pedidos;
    private final MensajeRepository mensajes;
    private final VehiculoRepository vehiculos;
    private final EquipoService equipo;

    public ResumenService(NegocioContexto contexto, PedidoRepository pedidos, MensajeRepository mensajes,
                          VehiculoRepository vehiculos, EquipoService equipo) {
        this.contexto = contexto;
        this.pedidos = pedidos;
        this.mensajes = mensajes;
        this.vehiculos = vehiculos;
        this.equipo = equipo;
    }

    @Transactional(readOnly = true)
    public ResumenDTO resumen(UsuarioPrincipal yo) {
        Negocio negocio = contexto.negocioDe(yo);
        Long negocioId = negocio.getId();
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();

        long sinAsignar = pedidos.countByNegocioIdAndEstado(negocioId, EstadoPedido.PENDIENTE);
        long enCamino = pedidos.countByNegocioIdAndEstado(negocioId, EstadoPedido.EN_CAMINO);
        long entregadosHoy = pedidos.countByNegocioIdAndEstadoAndActualizadoEnGreaterThanEqual(negocioId,
                EstadoPedido.ENTREGADO, inicioHoy);
        long mensajesNuevos = mensajes.countByDestinatarioIdAndLeidoFalse(yo.getId());
        List<RepartidorHoyDTO> repartidoresHoy = equipo.repartidoresConRutaHoy(negocio);

        List<AtencionDTO> atencion = new ArrayList<>();
        for (Pedido p : pedidos.findTop5ByNegocioIdAndEstadoOrderByActualizadoEnDesc(negocioId, EstadoPedido.NO_ENTREGADO)) {
            atencion.add(new AtencionDTO("NO_ENTREGADO", "Pedido #" + p.getId() + " no entregado",
                    p.getClienteNombre() + " · " + p.getZona(), p.getId()));
        }
        Set<Long> pedidosConMensaje = new LinkedHashSet<>();
        for (Mensaje m : mensajes.noLeidos(yo.getId(), PageRequest.of(0, 20))) {
            Long pedidoId = m.getPedido().getId();
            if (pedidosConMensaje.size() < MAXIMO_POR_TIPO && pedidosConMensaje.add(pedidoId)) {
                atencion.add(new AtencionDTO("MENSAJE", "Mensaje de " + m.getRemitente().getNombre(),
                        "Sobre el pedido #" + pedidoId, pedidoId));
            }
        }
        List<Vehiculo> porVencer = vehiculos.findByNegocioIdAndSeguroVenceLessThanEqualOrderBySeguroVence(negocioId,
                hoy.plusDays(7));
        for (Vehiculo v : porVencer.stream().limit(MAXIMO_POR_TIPO).toList()) {
            long dias = ChronoUnit.DAYS.between(hoy, v.getSeguroVence());
            String cuando = dias < 0 ? "venció el " + v.getSeguroVence()
                    : dias == 0 ? "vence hoy" : "vence en " + dias + (dias == 1 ? " día" : " días");
            atencion.add(new AtencionDTO("SEGURO", dias < 0 ? "Seguro vencido" : "Seguro por vencer",
                    EtiquetasVehiculo.etiqueta(v.getTipo()) + " " + v.getPlaca() + " · " + cuando, v.getId()));
        }
        return new ResumenDTO(sinAsignar, enCamino, entregadosHoy, mensajesNuevos, repartidoresHoy, atencion);
    }
}
