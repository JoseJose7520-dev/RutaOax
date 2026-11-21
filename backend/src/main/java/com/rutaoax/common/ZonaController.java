package com.rutaoax.common;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zonas")
public class ZonaController {

    @GetMapping
    public List<Zona> listar() {
        return ZonaCatalogo.todas();
    }
}
