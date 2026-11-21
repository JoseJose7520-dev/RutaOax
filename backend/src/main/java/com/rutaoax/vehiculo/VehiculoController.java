package com.rutaoax.vehiculo;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.PageResponse;
import com.rutaoax.vehiculo.dto.VehiculoDTO;
import com.rutaoax.vehiculo.dto.VehiculoDisponibleDTO;
import com.rutaoax.vehiculo.dto.VehiculoRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService servicio;

    public VehiculoController(VehiculoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public PageResponse<VehiculoDTO> listar(@AuthenticationPrincipal UsuarioPrincipal yo,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) TipoVehiculo tipo,
                                            @RequestParam(required = false) EstadoVehiculo estado,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return servicio.listar(yo, q, tipo, estado, page, size);
    }

    @GetMapping("/disponibles")
    public List<VehiculoDisponibleDTO> disponibles(@AuthenticationPrincipal UsuarioPrincipal yo,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.disponibles(yo, fecha);
    }

    @GetMapping("/{id}")
    public VehiculoDTO obtener(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        return servicio.obtener(yo, id);
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crear(@AuthenticationPrincipal UsuarioPrincipal yo,
                                             @Valid @RequestBody VehiculoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(yo, req));
    }

    @PutMapping("/{id}")
    public VehiculoDTO actualizar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id,
                                  @Valid @RequestBody VehiculoRequest req) {
        return servicio.actualizar(yo, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        servicio.eliminar(yo, id);
        return ResponseEntity.noContent().build();
    }
}
