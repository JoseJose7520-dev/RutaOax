package com.rutaoax.recorrido;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.ForbiddenException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.negocio.NegocioContexto;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.notificacion.NotificacionService;
import com.rutaoax.pedido.EstadoPedido;
import com.rutaoax.pedido.Pedido;
import com.rutaoax.pedido.PedidoRepository;
import com.rutaoax.pedido.dto.PedidoDTO;
import com.rutaoax.recorrido.dto.RecorridoDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Lo que pasa después de asignar: el repartidor consulta su ruta, la inicia y marca cada entrega.
 * El negocio puede consultar los recorridos de su equipo; el administrador, todos.
 */
@Service
public class EntregaService {

    private final RecorridoRepository recorridos;
    private final PedidoRepository pedidos;
    private final RepartidorRepository repartidores;
    private final RecorridoService recorridoService;
    private final NegocioContexto contexto;
    private final NotificacionService notificaciones;

    public EntregaService(RecorridoRepository recorridos, PedidoRepository pedidos, RepartidorRepository repartidores,
                          RecorridoService recorridoService, NegocioContexto contexto,
                          NotificacionService notificaciones) {
        this.recorridos = recorridos;
        this.pedidos = pedidos;
        this.repartidores = repartidores;
        this.recorridoService = recorridoService;
        this.contexto = contexto;
        this.notificaciones = notificaciones;
    }

    @Transactional(readOnly = true)
    public PageResponse<RecorridoDTO> listar(UsuarioPrincipal yo, LocalDate fecha, EstadoRecorrido estado,
                                             int page, int size) {
        Set<String> roles = yo.getRoles();
        Long negocioId = null;
        Long usuarioId = null;
        if (roles.contains("ADMIN")) {
            // ve todos
        } else if (roles.contains("NEGOCIO")) {
            negocioId = contexto.negocioDe(yo).getId();
        } else if (roles.contains("REPARTIDOR")) {
            usuarioId = yo.getId();
        } else {
            throw new ForbiddenException("No tienes permiso para ver recorridos");
        }
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("fecha").descending().and(Sort.by("id").descending()));
        Page<Recorrido> pagina = recorridos.buscar(negocioId, usuarioId, fecha, estado, pageable);
        return PageResponse.from(pagina.map(r -> recorridoService.aDTO(r.getId())));
    }

    @Transactional(readOnly = true)
    public RecorridoDTO deHoy(UsuarioPrincipal yo) {
        Repartidor yoRepartidor = repartidorDe(yo);
        Recorrido r = recorridos.findByRepartidorIdAndFecha(yoRepartidor.getId(), LocalDate.now())
                .orElseThrow(() -> new NotFoundException("Hoy no tienes entregas asignadas"));
        return recorridoService.aDTO(r.getId());
    }

    @Transactional(readOnly = true)
    public RecorridoDTO detalle(UsuarioPrincipal yo, Long id) {
        Recorrido r = recorridos.detalle(id).orElseThrow(() -> new NotFoundException("El recorrido no existe"));
        if (!puedeVer(yo, r)) {
            throw new NotFoundException("El recorrido no existe");
        }
        return recorridoService.aDTO(r.getId());
    }

    /** PLANEADO → EN_CURSO. Sus pedidos pasan de ASIGNADO a EN_CAMINO. */
    @Transactional
    public RecorridoDTO iniciar(UsuarioPrincipal yo, Long id) {
        Recorrido r = recorridoPropio(yo, id);
        if (r.getEstado() != EstadoRecorrido.PLANEADO) {
            throw new ConflictException("Este recorrido ya se inició o ya terminó");
        }
        if (!r.getFecha().equals(LocalDate.now())) {
            throw new ConflictException("Solo puedes iniciar el recorrido de hoy");
        }
        List<Pedido> paradas = pedidos.findByRecorridoIdOrderByOrdenParada(r.getId());
        if (paradas.isEmpty()) {
            throw new ConflictException("El recorrido no tiene paradas");
        }
        String repartidor = r.getRepartidor().getUsuario().getNombre();
        for (Pedido p : paradas) {
            if (p.getEstado() == EstadoPedido.ASIGNADO) {
                p.setEstado(EstadoPedido.EN_CAMINO);
                // HU-17: SMS a cada cliente cuando su pedido sale
                Long pedidoId = p.getId();
                String telefono = p.getClienteTelefono();
                String negocio = NotificacionService.nombreNegocio(p);
                Long clienteId = p.getCliente() != null ? p.getCliente().getId() : null;
                despuesDeGuardar(() -> notificaciones.pedidoEnCamino(pedidoId, telefono, negocio, repartidor, clienteId));
            }
        }
        pedidos.saveAll(paradas);
        r.setEstado(EstadoRecorrido.EN_CURSO);
        recorridos.save(r);
        return recorridoService.aDTO(r.getId());
    }

    /** EN_CAMINO → ENTREGADO o NO_ENTREGADO. Si ya no quedan paradas en camino, el recorrido se finaliza. */
    @Transactional
    public PedidoDTO marcar(UsuarioPrincipal yo, Long pedidoId, EstadoPedido nuevo, String motivo) {
        if (nuevo != EstadoPedido.ENTREGADO && nuevo != EstadoPedido.NO_ENTREGADO) {
            throw new ConflictException("Solo se puede marcar como entregado o no entregado");
        }
        Pedido p = pedidos.detalle(pedidoId).orElseThrow(() -> new NotFoundException("El pedido no existe"));
        Recorrido r = p.getRecorrido();
        if (r == null || !r.getRepartidor().getUsuario().getId().equals(yo.getId())) {
            throw new NotFoundException("El pedido no existe");
        }
        if (p.getEstado() != EstadoPedido.EN_CAMINO) {
            throw new ConflictException("Solo puedes marcar un pedido que va en camino (inicia primero tu recorrido)");
        }
        p.setEstado(nuevo);
        if (nuevo == EstadoPedido.NO_ENTREGADO && motivo != null && !motivo.isBlank()) {
            p.setNotas(recortar("No entregado: " + motivo.trim()
                    + (p.getNotas() == null || p.getNotas().isBlank() ? "" : " | " + p.getNotas())));
        }
        pedidos.save(p);
        avisarCliente(p, nuevo, motivo);

        boolean quedanEnCamino = pedidos.findByRecorridoIdOrderByOrdenParada(r.getId()).stream()
                .anyMatch(x -> x.getEstado() == EstadoPedido.EN_CAMINO || x.getEstado() == EstadoPedido.ASIGNADO);
        if (!quedanEnCamino) {
            r.setEstado(EstadoRecorrido.FINALIZADO);
            recorridos.save(r);
        }
        return PedidoDTO.from(p);
    }

    private Repartidor repartidorDe(UsuarioPrincipal yo) {
        if (!yo.getRoles().contains("REPARTIDOR")) {
            throw new ForbiddenException("Esta acción es solo para repartidores");
        }
        return repartidores.findByUsuarioId(yo.getId())
                .orElseThrow(() -> new ForbiddenException("Esta acción es solo para repartidores"));
    }

    private Recorrido recorridoPropio(UsuarioPrincipal yo, Long id) {
        repartidorDe(yo);
        Recorrido r = recorridos.detalle(id).orElseThrow(() -> new NotFoundException("El recorrido no existe"));
        if (!r.getRepartidor().getUsuario().getId().equals(yo.getId())) {
            throw new NotFoundException("El recorrido no existe");
        }
        return r;
    }

    private boolean puedeVer(UsuarioPrincipal yo, Recorrido r) {
        Set<String> roles = yo.getRoles();
        if (roles.contains("ADMIN")) {
            return true;
        }
        if (roles.contains("NEGOCIO")) {
            Long negocioId = contexto.negocioDe(yo).getId();
            return r.getRepartidor().getNegocio() != null && r.getRepartidor().getNegocio().getId().equals(negocioId);
        }
        if (roles.contains("REPARTIDOR")) {
            return r.getRepartidor().getUsuario().getId().equals(yo.getId());
        }
        return false;
    }

    /** HU-18: WhatsApp al cliente al cerrar su parada (y correo si tiene cuenta y se entregó). */
    private void avisarCliente(Pedido p, EstadoPedido estado, String motivo) {
        Long pedidoId = p.getId();
        String telefono = p.getClienteTelefono();
        String negocio = NotificacionService.nombreNegocio(p);
        Long clienteId = p.getCliente() != null ? p.getCliente().getId() : null;
        String email = p.getCliente() != null ? p.getCliente().getEmail() : null;
        String nombre = p.getCliente() != null ? p.getCliente().getNombre() : p.getClienteNombre();
        if (estado == EstadoPedido.ENTREGADO) {
            despuesDeGuardar(() -> notificaciones.pedidoEntregado(pedidoId, telefono, negocio, clienteId, email, nombre));
        } else {
            despuesDeGuardar(() -> notificaciones.pedidoNoEntregado(pedidoId, telefono, negocio, motivo, clienteId));
        }
    }

    /** Los avisos salen solo si el cambio se guardó; si la transacción falla, no se manda nada. */
    private static void despuesDeGuardar(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }

    private static String recortar(String texto) {
        return texto.length() <= 255 ? texto : texto.substring(0, 255);
    }
}
