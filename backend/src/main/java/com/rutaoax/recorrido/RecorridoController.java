package com.rutaoax.recorrido;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.PageResponse;
import com.rutaoax.recorrido.dto.RecorridoDTO;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recorridos")
public class RecorridoController {

    private final EntregaService servicio;

    public RecorridoController(EntregaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public PageResponse<RecorridoDTO> listar(@AuthenticationPrincipal UsuarioPrincipal yo,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                             @RequestParam(required = false) EstadoRecorrido estado,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return servicio.listar(yo, fecha, estado, page, size);
    }

    @GetMapping("/hoy")
    @PreAuthorize("hasRole('REPARTIDOR')")
    public RecorridoDTO hoy(@AuthenticationPrincipal UsuarioPrincipal yo) {
        return servicio.deHoy(yo);
    }

    @GetMapping("/{id}")
    public RecorridoDTO detalle(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        return servicio.detalle(yo, id);
    }

    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('REPARTIDOR')")
    public RecorridoDTO iniciar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        return servicio.iniciar(yo, id);
    }
}
