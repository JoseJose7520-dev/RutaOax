package com.rutaoax.auth.dto;

import com.rutaoax.usuario.dto.UsuarioDTO;
import java.time.LocalDateTime;

public record LoginResponse(String token, String tipo, LocalDateTime expiraEn, UsuarioDTO usuario) {
}
