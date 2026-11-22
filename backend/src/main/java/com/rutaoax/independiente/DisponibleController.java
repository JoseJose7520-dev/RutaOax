package com.rutaoax.independiente;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.PageResponse;
import com.rutaoax.independiente.dto.DisponibleDTO;
import com.rutaoax.recorrido.dto.RecorridoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disponibles")
public class DisponibleController {

    private final DisponibleService servicio;

    public DisponibleController(DisponibleService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public PageResponse<DisponibleDTO> listar(@AuthenticationPrincipal UsuarioPrincipal yo,
                                              @RequestParam(required = false) String zona,
                                              @RequestParam(required = false) Double maxKm,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return servicio.listar(yo, zona, maxKm, page, size);
    }

    @PostMapping("/{pedidoId}/tomar")
    public RecorridoDTO tomar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long pedidoId) {
        return servicio.tomar(yo, pedidoId);
    }

    @PostMapping("/{pedidoId}/soltar")
    public ResponseEntity<Void> soltar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long pedidoId) {
        servicio.soltar(yo, pedidoId);
        return ResponseEntity.ok().build();
    }
}
