package com.rutaoax.negocio;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ForbiddenException;
import org.springframework.stereotype.Component;

/** Resuelve a qué negocio pertenece el usuario autenticado. Se reutiliza en todos los módulos del negocio. */
@Component
public class NegocioContexto {

    private final NegocioRepository negocios;

    public NegocioContexto(NegocioRepository negocios) {
        this.negocios = negocios;
    }

    /** Negocio del usuario; falla si la cuenta no es de un negocio (por ejemplo, un administrador). */
    public Negocio negocioDe(UsuarioPrincipal yo) {
        return negocios.findByPropietarioId(yo.getId())
                .orElseThrow(() -> new ForbiddenException("Esta acción es solo para cuentas de negocio"));
    }

    /** Id del negocio para filtrar consultas; null significa "sin filtro" (administrador). */
    public Long ambito(UsuarioPrincipal yo) {
        if (yo.getRoles().contains("ADMIN")) {
            return null;
        }
        return negocioDe(yo).getId();
    }
}
