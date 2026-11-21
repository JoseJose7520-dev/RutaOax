package com.rutaoax.vehiculo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    @Query("select v from Vehiculo v where (:negocioId is null or v.negocio.id = :negocioId)"
            + " and lower(v.placa) like lower(concat('%', :q, '%'))"
            + " and (:tipo is null or v.tipo = :tipo)"
            + " and (:estado is null or v.estado = :estado)")
    Page<Vehiculo> buscar(@Param("negocioId") Long negocioId, @Param("q") String q,
                          @Param("tipo") TipoVehiculo tipo, @Param("estado") EstadoVehiculo estado,
                          Pageable pageable);

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    List<Vehiculo> findByNegocioIdAndEstadoOrderByPlaca(Long negocioId, EstadoVehiculo estado);
}
