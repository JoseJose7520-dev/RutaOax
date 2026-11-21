package com.rutaoax.pedido;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.negocio.NegocioContexto;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.pedido.dto.NegocioRefDTO;
import com.rutaoax.pedido.dto.SeguimientoDTO;
import com.rutaoax.pedido.dto.SeguimientoDTO.Paso;
import com.rutaoax.pedido.dto.SeguimientoDTO.RepartidorSeguimiento;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeguimientoService {

    private final PedidoRepository pedidos;
    private final NegocioContexto contexto;

    public SeguimientoService(PedidoRepository pedidos, NegocioContexto contexto) {
        this.pedidos = pedidos;
        this.contexto = contexto;
    }

    /** Lo pueden ver el cliente dueño, el negocio dueño y el administrador. A cualquier otro se le responde 404. */
    @Transactional(readOnly = true)
    public SeguimientoDTO seguimiento(UsuarioPrincipal yo, Long id) {
        Pedido p = pedidos.detalle(id).orElseThrow(() -> new NotFoundException("El pedido no existe"));
        if (!puedeVer(yo, p)) {
            throw new NotFoundException("El pedido no existe");
        }
        EstadoPedido estado = p.getEstado();

        NegocioRefDTO negocio = p.getNegocio() == null ? null
                : new NegocioRefDTO(p.getNegocio().getId(), p.getNegocio().getNombre());
        RepartidorSeguimiento repartidor = null;
        int paradasAntes = 0;
        if (p.getRecorrido() != null) {
            Repartidor rep = p.getRecorrido().getRepartidor();
            repartidor = new RepartidorSeguimiento(rep.getUsuario().getNombre(),
                    rep.getNegocio() == null ? "INDEPENDIENTE" : "EMPRESA");
            if ((estado == EstadoPedido.ASIGNADO || estado == EstadoPedido.EN_CAMINO) && p.getOrdenParada() != null) {
                paradasAntes = (int) pedidos.findByRecorridoIdOrderByOrdenParada(p.getRecorrido().getId()).stream()
                        .filter(x -> x.getOrdenParada() != null && x.getOrdenParada() < p.getOrdenParada())
                        .filter(x -> x.getEstado() == EstadoPedido.ASIGNADO || x.getEstado() == EstadoPedido.EN_CAMINO)
                        .count();
            }
        }

        boolean asignado = estado != EstadoPedido.PENDIENTE;
        boolean enCamino = estado == EstadoPedido.EN_CAMINO || estado == EstadoPedido.ENTREGADO
                || estado == EstadoPedido.NO_ENTREGADO;
        List<Paso> pasos = new ArrayList<>();
        pasos.add(new Paso("RECIBIDO", true, p.getCreadoEn()));
        pasos.add(new Paso("ASIGNADO", asignado, null));
        pasos.add(new Paso("EN_CAMINO", enCamino, null));
        if (estado == EstadoPedido.NO_ENTREGADO) {
            pasos.add(new Paso("NO_ENTREGADO", true, p.getActualizadoEn()));
        } else {
            boolean entregado = estado == EstadoPedido.ENTREGADO;
            pasos.add(new Paso("ENTREGADO", entregado, entregado ? p.getActualizadoEn() : null));
        }
        return new SeguimientoDTO(p.getId(), estado, negocio, repartidor, paradasAntes, pasos);
    }

    private boolean puedeVer(UsuarioPrincipal yo, Pedido p) {
        Set<String> roles = yo.getRoles();
        if (roles.contains("ADMIN")) {
            return true;
        }
        if (roles.contains("NEGOCIO")) {
            Long negocioId = contexto.negocioDe(yo).getId();
            return p.getNegocio() != null && p.getNegocio().getId().equals(negocioId);
        }
        if (roles.contains("CLIENTE")) {
            return p.getCliente() != null && p.getCliente().getId().equals(yo.getId());
        }
        return false;
    }
}
