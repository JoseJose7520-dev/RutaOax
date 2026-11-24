package com.rutaoax.vehiculo;

import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioRepository;
import com.rutaoax.vehiculo.dto.VehiculoDTO;
import com.rutaoax.vehiculo.dto.VehiculoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vehiculos de todos los negocios para el administrador.
 * Editar y eliminar usan PUT y DELETE de /api/vehiculos/{id}, que ya permiten al ADMIN.
 */
@RestController
@RequestMapping("/api/admin/vehiculos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVehiculoController {

    private final VehiculoService servicio;
    private final NegocioRepository negocios;

    public AdminVehiculoController(VehiculoService servicio, NegocioRepository negocios) {
        this.servicio = servicio;
        this.negocios = negocios;
    }

    @GetMapping
    public PageResponse<VehiculoDTO> listar(@RequestParam(required = false) Long negocioId,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) TipoVehiculo tipo,
                                            @RequestParam(required = false) EstadoVehiculo estado,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return servicio.listarAdmin(negocioId, q, tipo, estado, page, size);
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crear(@RequestParam Long negocioId, @Valid @RequestBody VehiculoRequest req) {
        Negocio negocio = negocios.findById(negocioId).orElseThrow(() -> new NotFoundException("El negocio no existe"));
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crearPara(negocio, req));
    }
}
