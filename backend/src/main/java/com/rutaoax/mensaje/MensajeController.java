package com.rutaoax.mensaje;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.PageResponse;
import com.rutaoax.mensaje.dto.HiloDTO;
import com.rutaoax.mensaje.dto.MensajeDTO;
import com.rutaoax.mensaje.dto.MensajeRequest;
import com.rutaoax.mensaje.dto.NoLeidosDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final MensajeService servicio;

    public MensajeController(MensajeService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/hilos")
    public PageResponse<HiloDTO> hilos(@AuthenticationPrincipal UsuarioPrincipal yo,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return servicio.hilos(yo, page, size);
    }

    @GetMapping("/pedidos/{pedidoId}")
    public PageResponse<MensajeDTO> conversacion(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long pedidoId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return servicio.conversacion(yo, pedidoId, page, size);
    }

    @PostMapping("/pedidos/{pedidoId}")
    public ResponseEntity<MensajeDTO> enviar(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long pedidoId,
                                             @Valid @RequestBody MensajeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.enviar(yo, pedidoId, req.texto()));
    }

    @PatchMapping("/pedidos/{pedidoId}/leido")
    public ResponseEntity<Void> leido(@AuthenticationPrincipal UsuarioPrincipal yo, @PathVariable Long pedidoId) {
        servicio.marcarLeidos(yo, pedidoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/no-leidos")
    public NoLeidosDTO noLeidos(@AuthenticationPrincipal UsuarioPrincipal yo) {
        return servicio.noLeidos(yo);
    }
}
