package com.rutaoax.negocio.dto;

import com.rutaoax.usuario.dto.UsuarioDTO;

public record EquipoDTO(Long id, UsuarioDTO usuario, String vehiculoHoy, long entregadosHoy, long totalHoy,
                        String estadoHoy) {
}
