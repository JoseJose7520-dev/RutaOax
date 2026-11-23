package com.rutaoax.mensaje;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.mensaje.dto.HiloDTO;
import com.rutaoax.mensaje.dto.MensajeDTO;
import com.rutaoax.mensaje.dto.NoLeidosDTO;
import com.rutaoax.mensaje.dto.PersonaDTO;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.pedido.Pedido;
import com.rutaoax.pedido.PedidoRepository;
import com.rutaoax.usuario.Usuario;
import com.rutaoax.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-23 y HU-24: mensajes por pedido. Participan el cliente con cuenta, el dueño del negocio del pedido
 * y, si el pedido no es de un negocio, el repartidor independiente que lo lleva.
 * Quien escribe no elige destinatario: lo decide el backend.
 *  - El cliente le escribe al negocio; si el pedido no tiene negocio, al independiente asignado.
 *  - El negocio y el independiente le escriben al cliente.
 */
@Service
public class MensajeService {

    private final MensajeRepository mensajes;
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;

    public MensajeService(MensajeRepository mensajes, PedidoRepository pedidos, UsuarioRepository usuarios) {
        this.mensajes = mensajes;
        this.pedidos = pedidos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public PageResponse<HiloDTO> hilos(UsuarioPrincipal yo, int page, int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<Object[]> filas = mensajes.hilos(yo.getId(), pagina);
        List<Long> ids = filas.getContent().stream().map(f -> (Long) f[0]).toList();
        Map<Long, Mensaje> ultimo = ids.isEmpty() ? Collections.emptyMap()
                : mensajes.ultimos(yo.getId(), ids).stream()
                        .collect(Collectors.toMap(m -> m.getPedido().getId(), Function.identity(), (a, b) -> a));
        List<HiloDTO> hilos = new ArrayList<>();
        for (Object[] f : filas.getContent()) {
            Long pedidoId = (Long) f[0];
            Mensaje m = ultimo.get(pedidoId);
            Usuario otro = m == null ? null
                    : (m.getRemitente().getId().equals(yo.getId()) ? m.getDestinatario() : m.getRemitente());
            hilos.add(new HiloDTO(pedidoId, "Pedido #" + pedidoId,
                    otro == null ? null : new PersonaDTO(otro.getId(), otro.getNombre()),
                    m == null ? "" : m.getTexto(), (LocalDateTime) f[1], ((Number) f[2]).longValue()));
        }
        return PageResponse.from(new PageImpl<>(hilos, pagina, filas.getTotalElements()));
    }

    /** Página 0 = mensajes más recientes; dentro de la página van en orden cronológico. */
    @Transactional(readOnly = true)
    public PageResponse<MensajeDTO> conversacion(UsuarioPrincipal yo, Long pedidoId, int page, int size) {
        participante(yo, cargar(pedidoId));
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Mensaje> datos = mensajes.dePedido(pedidoId, yo.getId(), pagina);
        List<MensajeDTO> lista = new ArrayList<>(datos.getContent().stream().map(m -> MensajeDTO.from(m, yo.getId())).toList());
        Collections.reverse(lista);
        return PageResponse.from(new PageImpl<>(lista, pagina, datos.getTotalElements()));
    }

    @Transactional
    public MensajeDTO enviar(UsuarioPrincipal yo, Long pedidoId, String texto) {
        Pedido p = cargar(pedidoId);
        Rol rol = participante(yo, p);
        Usuario destino = switch (rol) {
            case CLIENTE -> {
                Usuario dueno = p.getNegocio() != null ? p.getNegocio().getPropietario() : independiente(p);
                if (dueno == null) {
                    throw new ConflictException("Tu pedido todavía no tiene un repartidor asignado; intenta más tarde");
                }
                yield dueno;
            }
            case NEGOCIO, INDEPENDIENTE -> {
                if (p.getCliente() == null) {
                    throw new ConflictException("El cliente de este pedido no tiene cuenta en RutaOax; llámale por teléfono");
                }
                yield p.getCliente();
            }
        };
        Mensaje m = new Mensaje();
        m.setPedido(p);
        m.setRemitente(usuarios.getReferenceById(yo.getId()));
        m.setDestinatario(destino);
        m.setTexto(texto.trim());
        m.setLeido(false);
        Mensaje guardado = mensajes.save(m);
        return new MensajeDTO(guardado.getId(), pedidoId, new PersonaDTO(yo.getId(), nombreDe(yo)), guardado.getTexto(),
                true, false, guardado.getCreadoEn() != null ? guardado.getCreadoEn() : LocalDateTime.now());
    }

    @Transactional
    public void marcarLeidos(UsuarioPrincipal yo, Long pedidoId) {
        participante(yo, cargar(pedidoId));
        mensajes.marcarLeidos(pedidoId, yo.getId());
    }

    @Transactional(readOnly = true)
    public NoLeidosDTO noLeidos(UsuarioPrincipal yo) {
        return new NoLeidosDTO(mensajes.countByDestinatarioIdAndLeidoFalse(yo.getId()));
    }

    private enum Rol { CLIENTE, NEGOCIO, INDEPENDIENTE }

    private Pedido cargar(Long pedidoId) {
        return pedidos.detalle(pedidoId).orElseThrow(() -> new NotFoundException("El pedido no existe"));
    }

    /** Qué papel tengo en este pedido. Si no participo, respondo 404 para no revelar que existe. */
    private Rol participante(UsuarioPrincipal yo, Pedido p) {
        Long id = yo.getId();
        if (p.getCliente() != null && p.getCliente().getId().equals(id)) {
            return Rol.CLIENTE;
        }
        if (p.getNegocio() != null && p.getNegocio().getPropietario() != null
                && p.getNegocio().getPropietario().getId().equals(id)) {
            return Rol.NEGOCIO;
        }
        Usuario ind = independiente(p);
        if (ind != null && ind.getId().equals(id)) {
            return Rol.INDEPENDIENTE;
        }
        throw new NotFoundException("El pedido no existe");
    }

    /** El repartidor independiente que lleva el pedido (solo si el pedido no es de un negocio). */
    private static Usuario independiente(Pedido p) {
        if (p.getNegocio() != null || p.getRecorrido() == null) {
            return null;
        }
        Repartidor rep = p.getRecorrido().getRepartidor();
        return rep.getNegocio() == null ? rep.getUsuario() : null;
    }

    private String nombreDe(UsuarioPrincipal yo) {
        return usuarios.findById(yo.getId()).map(Usuario::getNombre).orElse("");
    }
}
