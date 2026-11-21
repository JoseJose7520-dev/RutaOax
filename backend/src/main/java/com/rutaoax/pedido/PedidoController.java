package com.rutaoax.pedido;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.PageResponse;
import com.rutaoax.pedido.dto.AsignarRequest;
import com.rutaoax.pedido.dto.PedidoDTO;
import com.rutaoax.pedido.dto.PedidoRequest;
import com.rutaoax.recorrido.dto.RecorridoDTO;
import jakarta.validation.Valid;
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
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService servicio;
    private final AsignacionService asignacion;

    public PedidoController(PedidoService servicio, AsignacionService asignacion) {
        this.servicio = servicio;
        this.asignacion = asignacion;
    }

    @GetMapping
    public PageResponse<PedidoDTO> listar(@AuthenticationPrincipal UsuarioPrincipal yo,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(required = false) EstadoPedido estado,
                                          @RequestParam(required = false) String zona,
                                          @RequestParam(required = false) Modalidad modalidad,
                                          @RequestParam(required = false) Boolean asignado,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return servicio.listar(yo, q, estado, zona, modalidad, asignado, page, size);
    }

    @GetMapping("/{id}")
    public PedidoDTO obtener(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        return servicio.obtener(yo, id);
    }

    @PostMapping
    @PreAuthorize("hasRole('NEGOCIO')")
    public ResponseEntity<PedidoDTO> crear(@AuthenticationPrincipal UsuarioPrincipal yo,
                                           @Valid @RequestBody PedidoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(yo, req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('NEGOCIO','ADMIN')")
    public PedidoDTO actualizar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id,
                                @Valid @RequestBody PedidoRequest req) {
        return servicio.actualizar(yo, id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('NEGOCIO','ADMIN')")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        servicio.eliminar(yo, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/asignar")
    @PreAuthorize("hasRole('NEGOCIO')")
    public RecorridoDTO asignar(@AuthenticationPrincipal UsuarioPrincipal yo, @Valid @RequestBody AsignarRequest req) {
        return asignacion.asignar(yo, req);
    }

    @PostMapping("/{id}/publicar")
    @PreAuthorize("hasRole('NEGOCIO')")
    public PedidoDTO publicar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        return servicio.publicar(yo, id);
    }

    @PostMapping("/{id}/reasignar")
    @PreAuthorize("hasRole('NEGOCIO')")
    public PedidoDTO reasignar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        return asignacion.reasignar(yo, id);
    }

    @PostMapping("/{id}/desasignar")
    @PreAuthorize("hasRole('NEGOCIO')")
    public PedidoDTO desasignar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        return asignacion.desasignar(yo, id);
    }
}
