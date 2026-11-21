package com.rutaoax.negocio;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.PageResponse;
import com.rutaoax.negocio.dto.EquipoDTO;
import com.rutaoax.negocio.dto.EquipoRequest;
import com.rutaoax.negocio.dto.EquipoUpdateRequest;
import com.rutaoax.negocio.dto.RepartidorDisponibleDTO;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipo")
@PreAuthorize("hasRole('NEGOCIO')")
public class EquipoController {

    private final EquipoService servicio;

    public EquipoController(EquipoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public PageResponse<EquipoDTO> listar(@AuthenticationPrincipal UsuarioPrincipal yo,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return servicio.listar(yo, q, page, size);
    }

    @GetMapping("/disponibles")
    public List<RepartidorDisponibleDTO> disponibles(@AuthenticationPrincipal UsuarioPrincipal yo,
                                                     @RequestParam(required = false)
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.disponibles(yo, fecha);
    }

    @PostMapping
    public ResponseEntity<EquipoDTO> crear(@AuthenticationPrincipal UsuarioPrincipal yo,
                                           @Valid @RequestBody EquipoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(yo, req));
    }

    @PutMapping("/{repartidorId}")
    public EquipoDTO actualizar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long repartidorId,
                                @Valid @RequestBody EquipoUpdateRequest req) {
        return servicio.actualizar(yo, repartidorId, req);
    }

    @DeleteMapping("/{repartidorId}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long repartidorId) {
        servicio.eliminar(yo, repartidorId);
        return ResponseEntity.noContent().build();
    }
}
