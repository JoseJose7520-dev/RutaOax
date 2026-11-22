package com.rutaoax.independiente;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.ForbiddenException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.common.ZonaCatalogo;
import com.rutaoax.independiente.dto.DisponibleDTO;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.pedido.EstadoPedido;
import com.rutaoax.pedido.Pedido;
import com.rutaoax.pedido.PedidoRepository;
import com.rutaoax.recorrido.EstadoRecorrido;
import com.rutaoax.recorrido.Recorrido;
import com.rutaoax.recorrido.RecorridoRepository;
import com.rutaoax.recorrido.RecorridoService;
import com.rutaoax.recorrido.dto.RecorridoDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-19: el repartidor independiente ve las entregas abiertas, toma las que quiere y puede soltarlas. */
@Service
public class DisponibleService {

    /** Punto de referencia para la distancia: el Centro Histórico de Oaxaca. */
    private static final double LAT0 = 17.0609;
    private static final double LNG0 = -96.7253;
    /** Kilómetros por grado en esta latitud (aproximación plana, suficiente dentro de la ciudad). */
    private static final double KM_POR_GRADO_LAT = 110.57;
    private static final double KM_POR_GRADO_LNG = 111.32 * Math.cos(Math.toRadians(LAT0));

    private final DisponibleRepository disponibles;
    private final PedidoRepository pedidos;
    private final RecorridoRepository recorridos;
    private final RepartidorRepository repartidores;
    private final RecorridoService recorridoService;

    public DisponibleService(DisponibleRepository disponibles, PedidoRepository pedidos, RecorridoRepository recorridos,
                             RepartidorRepository repartidores, RecorridoService recorridoService) {
        this.disponibles = disponibles;
        this.pedidos = pedidos;
        this.recorridos = recorridos;
        this.repartidores = repartidores;
        this.recorridoService = recorridoService;
    }

    @Transactional(readOnly = true)
    public PageResponse<DisponibleDTO> listar(UsuarioPrincipal yo, String zona, Double maxKm, int page, int size) {
        if (!yo.getRoles().contains("ADMIN")) {
            independiente(yo);
        }
        String filtroZona = zona == null || zona.isBlank() ? null : zona;
        if (filtroZona != null && !ZonaCatalogo.existe(filtroZona)) {
            filtroZona = "__ninguna__";
        }
        BigDecimal limite = BigDecimal.valueOf(maxKm == null || maxKm <= 0 ? 9999 : maxKm);
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("creadoEn").ascending().and(Sort.by("id").ascending()));
        return PageResponse.from(disponibles.buscar(filtroZona, BigDecimal.valueOf(LAT0), BigDecimal.valueOf(LNG0),
                        BigDecimal.valueOf(KM_POR_GRADO_LNG), BigDecimal.valueOf(KM_POR_GRADO_LAT), limite, pagina)
                .map(p -> DisponibleDTO.from(p, distanciaKm(p))));
    }

    /** Si dos repartidores la toman al mismo tiempo, gana el primero: la actualización es condicional. */
    @Transactional
    public RecorridoDTO tomar(UsuarioPrincipal yo, Long pedidoId) {
        Repartidor rep = independiente(yo);
        LocalDate hoy = LocalDate.now();
        Recorrido ruta = recorridos.findByRepartidorIdAndFecha(rep.getId(), hoy).orElseGet(() -> {
            Recorrido nuevo = new Recorrido();
            nuevo.setRepartidor(rep);
            nuevo.setFecha(hoy);
            nuevo.setEstado(EstadoRecorrido.PLANEADO);
            return recorridos.save(nuevo);
        });
        int orden = pedidos.findByRecorridoIdOrderByOrdenParada(ruta.getId()).size() + 1;
        int cambiados = disponibles.tomarSiSigueLibre(pedidoId, ruta, orden);
        if (cambiados == 0) {
            if (pedidos.findById(pedidoId).isEmpty()) {
                throw new NotFoundException("La entrega no existe");
            }
            throw new ConflictException("Otro repartidor ya tomó esta entrega o ya no está disponible");
        }
        // Si ya andaba en ruta, la nueva parada sale directo "en camino"; si ya había terminado, la ruta se reabre.
        Recorrido actual = recorridos.findById(ruta.getId()).orElseThrow();
        if (actual.getEstado() != EstadoRecorrido.PLANEADO) {
            Pedido p = pedidos.findById(pedidoId).orElseThrow();
            p.setEstado(EstadoPedido.EN_CAMINO);
            pedidos.save(p);
            if (actual.getEstado() == EstadoRecorrido.FINALIZADO) {
                actual.setEstado(EstadoRecorrido.EN_CURSO);
                recorridos.save(actual);
            }
        }
        return recorridoService.aDTO(ruta.getId());
    }

    /** Solo se puede soltar una entrega propia que todavía no sale (ASIGNADO). Vuelve a quedar disponible. */
    @Transactional
    public void soltar(UsuarioPrincipal yo, Long pedidoId) {
        independiente(yo);
        Pedido p = pedidos.detalle(pedidoId).orElseThrow(() -> new NotFoundException("La entrega no existe"));
        Recorrido r = p.getRecorrido();
        if (r == null || !r.getRepartidor().getUsuario().getId().equals(yo.getId())) {
            throw new NotFoundException("La entrega no existe");
        }
        if (p.getEstado() != EstadoPedido.ASIGNADO) {
            throw new ConflictException("Solo puedes soltar una entrega que todavía no sale");
        }
        p.setEstado(EstadoPedido.PENDIENTE);
        p.setRecorrido(null);
        p.setOrdenParada(null);
        pedidos.save(p);
        List<Pedido> restantes = pedidos.findByRecorridoIdOrderByOrdenParada(r.getId());
        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).setOrdenParada(i + 1);
        }
        pedidos.saveAll(restantes);
    }

    private Repartidor independiente(UsuarioPrincipal yo) {
        Repartidor rep = yo.getRoles().contains("REPARTIDOR") ? repartidores.findByUsuarioId(yo.getId()).orElse(null) : null;
        if (rep == null || rep.getNegocio() != null) {
            throw new ForbiddenException("Esta sección es solo para repartidores independientes");
        }
        return rep;
    }

    private static double distanciaKm(Pedido p) {
        if (p.getLat() == null || p.getLng() == null) {
            return 0;
        }
        double dx = (p.getLng().doubleValue() - LNG0) * KM_POR_GRADO_LNG;
        double dy = (p.getLat().doubleValue() - LAT0) * KM_POR_GRADO_LAT;
        return Math.round(Math.sqrt(dx * dx + dy * dy) * 10) / 10.0;
    }
}
