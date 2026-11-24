package com.rutaoax.usuario;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.PageResponse;
import com.rutaoax.usuario.dto.AdminUsuarioDTO;
import com.rutaoax.usuario.dto.AdminUsuarioRequest;
import com.rutaoax.usuario.dto.AdminUsuarioUpdateRequest;
import com.rutaoax.usuario.dto.EstadoUsuarioRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Cuentas de todo el sistema (solo ADMIN). */
@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final AdminUsuarioService servicio;

    public AdminUsuarioController(AdminUsuarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public PageResponse<AdminUsuarioDTO> listar(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) RolNombre rol,
                                                @RequestParam(required = false) Boolean activo,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return servicio.listar(q, rol, activo, page, size);
    }

    @GetMapping("/{id}")
    public AdminUsuarioDTO obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    public ResponseEntity<AdminUsuarioDTO> crear(@Valid @RequestBody AdminUsuarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(req));
    }

    @PutMapping("/{id}")
    public AdminUsuarioDTO actualizar(@PathVariable Long id, @Valid @RequestBody AdminUsuarioUpdateRequest req) {
        return servicio.actualizar(id, req);
    }

    @PatchMapping("/{id}/activo")
    public AdminUsuarioDTO cambiarEstado(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id,
                                         @Valid @RequestBody EstadoUsuarioRequest req) {
        return servicio.cambiarEstado(yo.getId(), id, req.activo());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long id) {
        servicio.eliminar(yo.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
