package com.rutaoax.negocio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    Optional<Negocio> findByPropietarioId(Long usuarioId);
}
