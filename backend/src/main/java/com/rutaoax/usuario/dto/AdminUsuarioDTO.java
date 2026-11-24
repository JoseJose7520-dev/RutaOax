package com.rutaoax.usuario.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Fila de la tabla de usuarios del administrador (nunca incluye el hash de la contraseña). */
public record AdminUsuarioDTO(Long id, String nombre, String email, String telefono, boolean activo,
                              List<String> roles, PerfilDTO perfil, LocalDateTime creadoEn) {

    public static AdminUsuarioDTO de(UsuarioDTO u, LocalDateTime creadoEn) {
        return new AdminUsuarioDTO(u.id(), u.nombre(), u.email(), u.telefono(), u.activo(), u.roles(), u.perfil(), creadoEn);
    }
}
