package com.rutaoax.pedido;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.ForbiddenException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.common.ZonaCatalogo;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioContexto;
import com.rutaoax.pedido.dto.PedidoDTO;
import com.rutaoax.pedido.dto.PedidoRequest;
import com.rutaoax.recorrido.Recorrido;
import com.rutaoax.recorrido.RecorridoService;
import com.rutaoax.usuario.RolNombre;
import com.rutaoax.usuario.Usuario;
import com.rutaoax.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final NegocioContexto contexto;
    private final UsuarioRepository usuarios;
    private final RecorridoService recorridoService;

    public PedidoService(PedidoRepository pedidos, NegocioContexto contexto, UsuarioRepository usuarios,
                         RecorridoService recorridoService) {
        this.pedidos = pedidos;
        this.contexto = contexto;
        this.usuarios = usuarios;
        this.recorridoService = recorridoService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PedidoDTO> listar(UsuarioPrincipal yo, String q, EstadoPedido estado, String zona,
                                          Modalidad modalidad, Boolean asignado, int page, int size) {
        Set<String> roles = yo.getRoles();
        Long negocioId = null;
        Long clienteId = null;
        Long repUsuarioId = null;
        if (roles.contains("ADMIN")) {
            // sin filtro de pertenencia
        } else if (roles.contains("NEGOCIO")) {
            negocioId = contexto.negocioDe(yo).getId();
        } else if (roles.contains("CLIENTE")) {
            clienteId = yo.getId();
        } else if (roles.contains("REPARTIDOR")) {
            repUsuarioId = yo.getId();
        } else {
            throw new ForbiddenException("No tienes permiso para ver pedidos");
        }
        String busqueda = q == null ? "" : q.trim().replaceFirst("^#", "");
        String asig = asignado == null ? "" : (asignado ? "SI" : "NO");
        String zonaFiltro = zona == null || zona.isBlank() ? null : zona;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("id").descending());
        Page<Pedido> pagina = pedidos.buscar(negocioId, clienteId, repUsuarioId, busqueda, estado, zonaFiltro,
                modalidad, asig, pageable);
        return PageResponse.from(pagina.map(PedidoDTO::from));
    }

    @Transactional(readOnly = true)
    public PedidoDTO obtener(UsuarioPrincipal yo, Long id) {
        Pedido p = pedidos.detalle(id).orElseThrow(() -> new NotFoundException("El pedido no existe"));
        if (!visible(yo, p)) {
            throw new NotFoundException("El pedido no existe");
        }
        return PedidoDTO.from(p);
    }

    @Transactional
    public PedidoDTO crear(UsuarioPrincipal yo, PedidoRequest req) {
        Negocio negocio = contexto.negocioDe(yo);
        Pedido p = new Pedido();
        p.setNegocio(negocio);
        p.setModalidad(req.modalidad() == null ? Modalidad.NEGOCIO : req.modalidad());
        p.setEstado(EstadoPedido.PENDIENTE);
        aplicar(p, req);
        if (p.getModalidad() == Modalidad.ABIERTO) {
            p.setOrigen(vacioANulo(req.origen()) != null ? vacioANulo(req.origen()) : negocio.getNombre());
            p.setPagoRepartidor(req.pagoRepartidor());
        }
        Pedido guardado = pedidos.saveAndFlush(p);
        asignarCoordenadas(guardado);
        return PedidoDTO.from(guardado);
    }

    @Transactional
    public PedidoDTO actualizar(UsuarioPrincipal yo, Long id, PedidoRequest req) {
        Pedido p = buscarGestionable(yo, id);
        if (p.getEstado() != EstadoPedido.PENDIENTE && p.getEstado() != EstadoPedido.ASIGNADO) {
            throw new ConflictException("Solo se puede editar un pedido sin asignar o asignado");
        }
        aplicar(p, req);
        if (p.getModalidad() == Modalidad.ABIERTO) {
            p.setOrigen(vacioANulo(req.origen()) != null ? vacioANulo(req.origen()) : p.getOrigen());
            p.setPagoRepartidor(req.pagoRepartidor());
        }
        asignarCoordenadas(p);
        return PedidoDTO.from(pedidos.save(p));
    }

    @Transactional
    public void eliminar(UsuarioPrincipal yo, Long id) {
        Pedido p = buscarGestionable(yo, id);
        if (p.getEstado() == EstadoPedido.EN_CAMINO || p.getEstado() == EstadoPedido.ENTREGADO) {
            throw new ConflictException("No se puede eliminar un pedido que ya salió a ruta o fue entregado");
        }
        Recorrido recorrido = p.getRecorrido();
        pedidos.delete(p);
        pedidos.flush();
        if (recorrido != null) {
            recorridoService.compactar(recorrido);
        }
    }

    @Transactional
    public PedidoDTO publicar(UsuarioPrincipal yo, Long id) {
        Negocio negocio = contexto.negocioDe(yo);
        Pedido p = pedidos.detalle(id)
                .filter(x -> x.getNegocio() != null && x.getNegocio().getId().equals(negocio.getId()))
                .orElseThrow(() -> new NotFoundException("El pedido no existe"));
        if (p.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ConflictException("Solo se puede publicar un pedido sin asignar");
        }
        if (p.getModalidad() == Modalidad.ABIERTO) {
            throw new ConflictException("Este pedido ya está publicado para repartidores independientes");
        }
        p.setModalidad(Modalidad.ABIERTO);
        if (p.getOrigen() == null) {
            p.setOrigen(negocio.getNombre());
        }
        return PedidoDTO.from(pedidos.save(p));
    }

    /** Pedido que el usuario puede modificar: del negocio del usuario, o cualquiera si es administrador. */
    private Pedido buscarGestionable(UsuarioPrincipal yo, Long id) {
        Long negocioId = contexto.ambito(yo);
        Pedido p = pedidos.detalle(id).orElseThrow(() -> new NotFoundException("El pedido no existe"));
        if (negocioId != null && (p.getNegocio() == null || !p.getNegocio().getId().equals(negocioId))) {
            throw new NotFoundException("El pedido no existe");
        }
        return p;
    }

    private boolean visible(UsuarioPrincipal yo, Pedido p) {
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
        if (roles.contains("REPARTIDOR")) {
            return p.getRecorrido() != null
                    && p.getRecorrido().getRepartidor().getUsuario().getId().equals(yo.getId());
        }
        return false;
    }

    private void aplicar(Pedido p, PedidoRequest req) {
        p.setClienteNombre(req.clienteNombre().trim());
        p.setClienteTelefono(vacioANulo(req.clienteTelefono()));
        Usuario clienteEnlazado = resolverCliente(req.clienteEmail());
        if (clienteEnlazado != null || p.getId() == null) {
            p.setCliente(clienteEnlazado);
        }
        p.setDireccion(req.direccion().trim());
        p.setZona(req.zona());
        p.setArticulos(req.articulos());
        p.setTotal(req.total());
        p.setNotas(vacioANulo(req.notas()));
        if (p.getId() == null) {
            double[] c = ZonaCatalogo.coordenadas(req.zona(), 0);
            p.setLat(redondear(c[0]));
            p.setLng(redondear(c[1]));
        }
    }

    private void asignarCoordenadas(Pedido p) {
        double[] c = ZonaCatalogo.coordenadas(p.getZona(), p.getId());
        p.setLat(redondear(c[0]));
        p.setLng(redondear(c[1]));
    }

    private BigDecimal redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(6, RoundingMode.HALF_UP);
    }

    private Usuario resolverCliente(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return usuarios.findByEmail(email.trim().toLowerCase())
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.CLIENTE))
                .orElse(null);
    }

    private String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
