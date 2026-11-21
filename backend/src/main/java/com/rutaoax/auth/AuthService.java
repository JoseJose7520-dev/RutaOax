package com.rutaoax.auth;

import com.rutaoax.auth.dto.LoginRequest;
import com.rutaoax.auth.dto.LoginResponse;
import com.rutaoax.auth.dto.RegisterRequest;
import com.rutaoax.auth.dto.TipoCuenta;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.UnauthorizedException;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioRepository;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.usuario.Rol;
import com.rutaoax.usuario.RolNombre;
import com.rutaoax.usuario.RolRepository;
import com.rutaoax.usuario.Usuario;
import com.rutaoax.usuario.UsuarioRepository;
import com.rutaoax.usuario.UsuarioService;
import com.rutaoax.usuario.dto.UsuarioDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final NegocioRepository negocios;
    private final RepartidorRepository repartidores;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UsuarioService usuarioService;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, NegocioRepository negocios,
                       RepartidorRepository repartidores, PasswordEncoder encoder, JwtService jwt,
                       UsuarioService usuarioService) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.negocios = negocios;
        this.repartidores = repartidores;
        this.encoder = encoder;
        this.jwt = jwt;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        String email = normalizar(req.email());
        Usuario u = usuarios.findByEmail(email).orElse(null);
        if (u == null || !u.isActivo() || !encoder.matches(req.password(), u.getPasswordHash())) {
            throw new UnauthorizedException("Correo o contraseña incorrectos");
        }
        String token = jwt.generateToken(u);
        return new LoginResponse(token, "Bearer", jwt.getExpiration(token), usuarioService.aDTO(u));
    }

    @Transactional
    public UsuarioDTO register(RegisterRequest req) {
        String email = normalizar(req.email());
        if (usuarios.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }
        TipoCuenta tipo = req.tipoCuenta();
        RolNombre rolNombre = switch (tipo) {
            case CLIENTE -> RolNombre.CLIENTE;
            case NEGOCIO -> RolNombre.NEGOCIO;
            case INDEPENDIENTE -> RolNombre.REPARTIDOR;
        };
        Rol rol = roles.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + rolNombre + " en la base de datos"));

        Usuario u = new Usuario();
        u.setNombre(req.nombre().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setTelefono(req.telefono() == null || req.telefono().isBlank() ? null : req.telefono());
        u.setActivo(true);
        u.getRoles().add(rol);
        try {
            u = usuarios.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Ya existe una cuenta con ese correo");
        }

        if (tipo == TipoCuenta.NEGOCIO) {
            Negocio n = new Negocio();
            n.setNombre(req.nombreNegocio().trim());
            n.setTelefono(u.getTelefono());
            n.setPropietario(u);
            negocios.save(n);
        } else if (tipo == TipoCuenta.INDEPENDIENTE) {
            Repartidor r = new Repartidor();
            r.setUsuario(u);
            r.setNegocio(null);
            repartidores.save(r);
        }
        return usuarioService.aDTO(u);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO me(Long usuarioId) {
        Usuario u = usuarios.findById(usuarioId).orElseThrow(() -> new NotFoundException("El usuario no existe"));
        return usuarioService.aDTO(u);
    }

    private String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
