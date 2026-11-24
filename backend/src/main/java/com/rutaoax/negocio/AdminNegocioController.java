package com.rutaoax.negocio;

import com.rutaoax.pedido.dto.NegocioRefDTO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lista corta de negocios para los filtros y formularios del administrador. */
@RestController
@RequestMapping("/api/admin/negocios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNegocioController {

    private final NegocioRepository negocios;

    public AdminNegocioController(NegocioRepository negocios) {
        this.negocios = negocios;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<NegocioRefDTO> listar() {
        return negocios.findAllByOrderByNombreAsc().stream()
                .map(n -> new NegocioRefDTO(n.getId(), n.getNombre()))
                .toList();
    }
}
