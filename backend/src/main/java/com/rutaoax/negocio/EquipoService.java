package com.rutaoax.negocio;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.negocio.dto.EquipoDTO;
import com.rutaoax.negocio.dto.EquipoRequest;
import com.rutaoax.negocio.dto.EquipoUpdateRequest;
import com.rutaoax.negocio.dto.RepartidorDisponibleDTO;
import com.rutaoax.pedido.PedidoRepository;
import com.rutaoax.recorrido.EstadoRecorrido;
import com.rutaoax.recorrido.Recorrido;
import com.rutaoax.recorrido.RecorridoRepository;
import com.rutaoax.usuario.Rol;
import com.rutaoax.usuario.RolNombre;
import com.rutaoax.usuario.RolRepository;
import com.rutaoax.usuario.Usuario;
import com.rutaoax.usuario.UsuarioRepository;
import com.rutaoax.usuario.dto.PerfilDTO;
import com.rutaoax.usuario.dto.UsuarioDTO;
import com.rutaoax.vehiculo.TipoVehiculo;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {

    private record ResumenDia(String vehiculo, long entregados, long total, boolean enCurso) {
    }

    private final NegocioContexto contexto;
    private final RepartidorRepository repartidores;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final RecorridoRepository recorridos;
    private final PedidoRepository pedidos;
    private final PasswordEncoder encoder;

    public EquipoService(NegocioContexto contexto, RepartidorRepository repartidores, UsuarioRepository usuarios,
                         RolRepository roles, RecorridoRepository recorridos, PedidoRepository pedidos,
                         PasswordEncoder encoder) {
        this.contexto = contexto;
        this.repartidores = repartidores;
        this.usuarios = usuarios;
        this.roles = roles;
        this.recorridos = recorridos;
        this.pedidos = pedidos;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipoDTO> listar(UsuarioPrincipal yo, String q, int page, int size) {
        Negocio negocio = contexto.negocioDe(yo);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                JpaSort.unsafe("u.nombre"));
        Page<Repartidor> pagina = repartidores.buscarEquipo(negocio.getId(), q == null ? "" : q.trim(), pageable);
        Map<Long, ResumenDia> resumen = resumenDeHoy(pagina.getContent().stream().map(Repartidor::getId).toList());
        return PageResponse.from(pagina.map(r -> aDTO(r, negocio, resumen.get(r.getId()))));
    }

    @Transactional
    public EquipoDTO crear(UsuarioPrincipal yo, EquipoRequest req) {
        Negocio negocio = contexto.negocioDe(yo);
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        Rol rol = roles.findByNombre(RolNombre.REPARTIDOR)
                .orElseThrow(() -> new IllegalStateException("Falta el rol REPARTIDOR en la base de datos"));
        Usuario u = new Usuario();
        u.setNombre(req.nombre().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setTelefono(vacioANulo(req.telefono()));
        u.setActivo(true);
        u.getRoles().add(rol);
        try {
            u = usuarios.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        Repartidor r = new Repartidor();
        r.setUsuario(u);
        r.setNegocio(negocio);
        return aDTO(repartidores.save(r), negocio, null);
    }

    @Transactional
    public EquipoDTO actualizar(UsuarioPrincipal yo, Long repartidorId, EquipoUpdateRequest req) {
        Negocio negocio = contexto.negocioDe(yo);
        Repartidor r = propio(negocio, repartidorId);
        Usuario u = r.getUsuario();
        u.setNombre(req.nombre().trim());
        u.setTelefono(vacioANulo(req.telefono()));
        usuarios.save(u);
        return aDTO(r, negocio, resumenDeHoy(List.of(r.getId())).get(r.getId()));
    }

    @Transactional
    public void eliminar(UsuarioPrincipal yo, Long repartidorId) {
        Negocio negocio = contexto.negocioDe(yo);
        Repartidor r = propio(negocio, repartidorId);
        if (recorridos.existsByRepartidorIdAndFechaGreaterThanEqualAndEstadoNot(r.getId(), LocalDate.now(),
                EstadoRecorrido.FINALIZADO)) {
            throw new ConflictException("Tiene recorridos pendientes. Reasigna sus pedidos antes de eliminarlo del equipo");
        }
        Usuario u = r.getUsuario();
        u.setActivo(false);
        usuarios.save(u);
    }

    @Transactional(readOnly = true)
    public List<RepartidorDisponibleDTO> disponibles(UsuarioPrincipal yo, LocalDate fecha) {
        Negocio negocio = contexto.negocioDe(yo);
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        Set<Long> ocupados = new HashSet<>(recorridos.repartidorIdsConRecorrido(dia));
        return repartidores.findByNegocioIdAndUsuarioActivoTrue(negocio.getId()).stream()
                .filter(r -> !ocupados.contains(r.getId()))
                .map(r -> new RepartidorDisponibleDTO(r.getId(), r.getUsuario().getNombre()))
                .sorted(Comparator.comparing(RepartidorDisponibleDTO::nombre))
                .toList();
    }

    private Repartidor propio(Negocio negocio, Long repartidorId) {
        Repartidor r = repartidores.findByIdAndNegocioId(repartidorId, negocio.getId())
                .orElseThrow(() -> new NotFoundException("El repartidor no existe"));
        if (!r.getUsuario().isActivo()) {
            throw new NotFoundException("El repartidor no existe");
        }
        return r;
    }

    /** Recorrido activo de hoy (PLANEADO o EN_CURSO) de cada repartidor, con su avance. */
    private Map<Long, ResumenDia> resumenDeHoy(List<Long> repartidorIds) {
        Map<Long, ResumenDia> resumen = new HashMap<>();
        if (repartidorIds.isEmpty()) {
            return resumen;
        }
        List<Recorrido> deHoy = recorridos.activosPorRepartidores(LocalDate.now(), repartidorIds);
        if (deHoy.isEmpty()) {
            return resumen;
        }
        Map<Long, long[]> conteos = new HashMap<>();
        for (Object[] fila : pedidos.conteoPorRecorrido(deHoy.stream().map(Recorrido::getId).toList())) {
            conteos.put((Long) fila[0], new long[]{((Number) fila[1]).longValue(), ((Number) fila[2]).longValue()});
        }
        for (Recorrido rec : deHoy) {
            long[] c = conteos.getOrDefault(rec.getId(), new long[]{0, 0});
            String vehiculo = rec.getVehiculo() == null ? null
                    : etiqueta(rec.getVehiculo().getTipo()) + " " + rec.getVehiculo().getPlaca();
            resumen.put(rec.getRepartidor().getId(),
                    new ResumenDia(vehiculo, c[1], c[0], rec.getEstado() == EstadoRecorrido.EN_CURSO));
        }
        return resumen;
    }

    private EquipoDTO aDTO(Repartidor r, Negocio negocio, ResumenDia resumen) {
        Usuario u = r.getUsuario();
        UsuarioDTO usuario = new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(), u.getAvatarUrl(),
                u.isActivo(), List.of(RolNombre.REPARTIDOR.name()),
                new PerfilDTO(negocio.getId(), negocio.getNombre(), "EMPRESA"));
        if (resumen == null) {
            return new EquipoDTO(r.getId(), usuario, null, 0, 0, "LIBRE");
        }
        return new EquipoDTO(r.getId(), usuario, resumen.vehiculo(), resumen.entregados(), resumen.total(),
                resumen.enCurso() ? "EN_RUTA" : "LIBRE");
    }

    private String etiqueta(TipoVehiculo tipo) {
        return switch (tipo) {
            case COMBI -> "Combi";
            case MOTOCICLETA -> "Motocicleta";
            case CAMIONETA -> "Camioneta";
            case BICICLETA -> "Bicicleta";
            case AUTOMOVIL -> "Automóvil";
        };
    }

    private String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
