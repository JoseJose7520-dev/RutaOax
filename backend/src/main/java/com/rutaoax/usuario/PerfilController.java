package com.rutaoax.usuario;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.usuario.dto.CambiarPasswordRequest;
import com.rutaoax.usuario.dto.PerfilRequest;
import com.rutaoax.usuario.dto.UsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public UsuarioDTO ver(@AuthenticationPrincipal UsuarioPrincipal yo) {
        return perfilService.obtener(yo.getId());
    }

    @PutMapping
    public UsuarioDTO actualizar(@AuthenticationPrincipal UsuarioPrincipal yo, @Valid @RequestBody PerfilRequest req) {
        return perfilService.actualizar(yo.getId(), req);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> cambiarPassword(@AuthenticationPrincipal UsuarioPrincipal yo,
                                                @Valid @RequestBody CambiarPasswordRequest req) {
        perfilService.cambiarPassword(yo.getId(), req);
        return ResponseEntity.noContent().build();
    }
}
