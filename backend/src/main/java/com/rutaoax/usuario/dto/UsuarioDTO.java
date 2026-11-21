package com.rutaoax.usuario.dto;

import java.util.List;

public record UsuarioDTO(Long id, String nombre, String email, String telefono, String avatarUrl, boolean activo,
                         List<String> roles, PerfilDTO perfil) {
}
