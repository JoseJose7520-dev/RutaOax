package com.rutaoax.negocio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {

    @EntityGraph(attributePaths = {"usuario", "negocio"})
    Optional<Repartidor> findByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = "usuario")
    Optional<Repartidor> findByIdAndNegocioId(Long id, Long negocioId);

    @EntityGraph(attributePaths = "usuario")
    List<Repartidor> findByNegocioIdAndUsuarioActivoTrue(Long negocioId);

    @Query(value = "select r from Repartidor r join fetch r.usuario u where r.negocio.id = :negocioId and u.activo = true"
            + " and (lower(u.nombre) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%')))",
            countQuery = "select count(r) from Repartidor r join r.usuario u where r.negocio.id = :negocioId and u.activo = true"
                    + " and (lower(u.nombre) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%')))")
    Page<Repartidor> buscarEquipo(@Param("negocioId") Long negocioId, @Param("q") String q, Pageable pageable);
}
