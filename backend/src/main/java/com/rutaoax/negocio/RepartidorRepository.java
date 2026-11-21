package com.rutaoax.negocio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    @EntityGraph(attributePaths = {"usuario", "negocio"})
    Optional<Repartidor> findByUsuarioId(Long usuarioId);
}
