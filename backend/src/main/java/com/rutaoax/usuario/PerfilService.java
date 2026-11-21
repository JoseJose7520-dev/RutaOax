package com.rutaoax.usuario;

import com.rutaoax.common.ConflictException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.usuario.dto.CambiarPasswordRequest;
import com.rutaoax.usuario.dto.PerfilRequest;
import com.rutaoax.usuario.dto.UsuarioDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;
    private final PasswordEncoder encoder;

    public PerfilService(UsuarioRepository usuarios, UsuarioService usuarioService, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtener(Long usuarioId) {
        return usuarioService.aDTO(buscar(usuarioId));
    }

    @Transactional
    public UsuarioDTO actualizar(Long usuarioId, PerfilRequest req) {
        Usuario u = buscar(usuarioId);
        u.setNombre(req.nombre().trim());
        u.setTelefono(vacioANulo(req.telefono()));
        u.setAvatarUrl(vacioANulo(req.avatarUrl()));
        return usuarioService.aDTO(usuarios.save(u));
    }

    @Transactional
    public void cambiarPassword(Long usuarioId, CambiarPasswordRequest req) {
        Usuario u = buscar(usuarioId);
        if (!encoder.matches(req.actual(), u.getPasswordHash())) {
            throw new ConflictException("La contraseña actual es incorrecta");
        }
        if (encoder.matches(req.nueva(), u.getPasswordHash())) {
            throw new ConflictException("La contraseña nueva debe ser distinta a la actual");
        }
        u.setPasswordHash(encoder.encode(req.nueva()));
        usuarios.save(u);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new NotFoundException("El usuario no existe"));
    }

    private String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
