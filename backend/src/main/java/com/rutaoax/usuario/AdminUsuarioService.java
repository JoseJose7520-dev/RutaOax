package com.rutaoax.usuario;

import com.rutaoax.common.ConflictException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioRepository;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.usuario.dto.AdminUsuarioDTO;
import com.rutaoax.usuario.dto.AdminUsuarioRequest;
import com.rutaoax.usuario.dto.AdminUsuarioUpdateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestion de cuentas desde el panel del administrador.
 * Reglas: nadie se desactiva ni se borra a si mismo, siempre queda al menos un ADMIN activo
 * y una cuenta con historial (pedidos o recorridos) solo se puede desactivar, no borrar.
 */
@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final NegocioRepository negocios;
    private final RepartidorRepository repartidores;
    private final PasswordEncoder encoder;
    private final UsuarioService usuarioService;

    public AdminUsuarioService(UsuarioRepository usuarios, RolRepository roles, NegocioRepository negocios,
                               RepartidorRepository repartidores, PasswordEncoder encoder, UsuarioService usuarioService) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.negocios = negocios;
        this.repartidores = repartidores;
        this.encoder = encoder;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUsuarioDTO> listar(String q, RolNombre rol, Boolean activo, int page, int size) {
        String texto = q == null || q.isBlank() ? null : "%" + q.trim().toLowerCase() + "%";
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("creadoEn").descending().and(Sort.by("id").descending()));
        return PageResponse.from(usuarios.buscarAdmin(texto, rol, activo, pagina).map(this::aDTO));
    }

    @Transactional(readOnly = true)
    public AdminUsuarioDTO obtener(Long id) {
        return aDTO(cargar(id));
    }

    @Transactional
    public AdminUsuarioDTO crear(AdminUsuarioRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        RolNombre rolNombre = switch (req.tipo()) {
            case "NEGOCIO" -> RolNombre.NEGOCIO;
            case "INDEPENDIENTE" -> RolNombre.REPARTIDOR;
            case "ADMIN" -> RolNombre.ADMIN;
            default -> RolNombre.CLIENTE;
        };
        Rol rol = roles.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + rolNombre + " en la base de datos"));

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

        // Igual que en el registro: el negocio y el repartidor independiente necesitan su registro propio
        if (rolNombre == RolNombre.NEGOCIO) {
            Negocio n = new Negocio();
            n.setNombre(req.nombreNegocio().trim());
            n.setTelefono(u.getTelefono());
            n.setPropietario(u);
            negocios.save(n);
        } else if (rolNombre == RolNombre.REPARTIDOR) {
            Repartidor r = new Repartidor();
            r.setUsuario(u);
            r.setNegocio(null);
            repartidores.save(r);
        }
        return aDTO(u);
    }

    @Transactional
    public AdminUsuarioDTO actualizar(Long id, AdminUsuarioUpdateRequest req) {
        Usuario u = cargar(id);
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("Ya existe otra cuenta con ese correo");
        }
        u.setNombre(req.nombre().trim());
        u.setEmail(email);
        u.setTelefono(vacioANulo(req.telefono()));
        if (req.nuevaPassword() != null && !req.nuevaPassword().isBlank()) {
            u.setPasswordHash(encoder.encode(req.nuevaPassword()));
        }
        return aDTO(usuarios.save(u));
    }

    @Transactional
    public AdminUsuarioDTO cambiarEstado(Long yoId, Long id, boolean activo) {
        Usuario u = cargar(id);
        if (!activo) {
            protegerAdmin(u, "desactivar");
            if (u.getId().equals(yoId)) {
                throw new ConflictException("No puedes desactivar tu propia cuenta");
            }
        }
        u.setActivo(activo);
        return aDTO(usuarios.save(u));
    }

    @Transactional
    public void eliminar(Long yoId, Long id) {
        Usuario u = cargar(id);
        protegerAdmin(u, "eliminar");
        if (u.getId().equals(yoId)) {
            throw new ConflictException("No puedes eliminar tu propia cuenta");
        }
        if (usuarios.contarPedidosDelNegocioDe(id) > 0 || usuarios.contarRecorridosDe(id) > 0) {
            throw new ConflictException("La cuenta tiene pedidos o recorridos registrados. Desactívala en lugar de eliminarla");
        }
        try {
            usuarios.delete(u);
            usuarios.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("La cuenta tiene información relacionada. Desactívala en lugar de eliminarla");
        }
    }

    // Si la cuenta es el unico ADMIN activo, quitarla dejaria el sistema sin administrador
    private void protegerAdmin(Usuario u, String accion) {
        boolean esAdmin = u.getRoles().stream().anyMatch(r -> r.getNombre() == RolNombre.ADMIN);
        if (esAdmin && u.isActivo() && usuarios.contarActivosConRol(RolNombre.ADMIN) <= 1) {
            throw new ConflictException("No se puede " + accion + " al único administrador del sistema");
        }
    }

    private Usuario cargar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new NotFoundException("El usuario no existe"));
    }

    private AdminUsuarioDTO aDTO(Usuario u) {
        return AdminUsuarioDTO.de(usuarioService.aDTO(u), u.getCreadoEn());
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
