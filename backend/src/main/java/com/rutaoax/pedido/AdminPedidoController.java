package com.rutaoax.pedido;

import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioRepository;
import com.rutaoax.pedido.dto.PedidoDTO;
import com.rutaoax.pedido.dto.PedidoRequest;
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
 * Pedidos de todo el sistema para el administrador.
 * Editar y eliminar usan PUT y DELETE de /api/pedidos/{id}, que ya permiten al ADMIN sin filtro de negocio.
 */
@RestController
@RequestMapping("/api/admin/pedidos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPedidoController {

    private final PedidoService servicio;
    private final NegocioRepository negocios;

    public AdminPedidoController(PedidoService servicio, NegocioRepository negocios) {
        this.servicio = servicio;
        this.negocios = negocios;
    }

    @GetMapping
    public PageResponse<PedidoDTO> listar(@RequestParam(required = false) Long negocioId,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(required = false) EstadoPedido estado,
                                          @RequestParam(required = false) String zona,
                                          @RequestParam(required = false) Modalidad modalidad,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return servicio.listarAdmin(negocioId, q, estado, zona, modalidad, page, size);
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crear(@RequestParam Long negocioId, @Valid @RequestBody PedidoRequest req) {
        Negocio negocio = negocios.findById(negocioId).orElseThrow(() -> new NotFoundException("El negocio no existe"));
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crearPara(negocio, req));
    }
}
