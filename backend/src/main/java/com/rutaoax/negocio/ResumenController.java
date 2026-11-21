package com.rutaoax.negocio;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.negocio.dto.ResumenDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/negocio")
@PreAuthorize("hasRole('NEGOCIO')")
public class ResumenController {

    private final ResumenService servicio;

    public ResumenController(ResumenService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    public ResumenDTO resumen(@AuthenticationPrincipal UsuarioPrincipal yo) {
        return servicio.resumen(yo);
    }
}
