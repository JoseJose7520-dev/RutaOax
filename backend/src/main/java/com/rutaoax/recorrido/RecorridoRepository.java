package com.rutaoax.recorrido;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecorridoRepository extends JpaRepository<Recorrido, Long> {

    @Query("select r from Recorrido r join fetch r.repartidor rep join fetch rep.usuario"
            + " where r.fecha = :fecha and r.vehiculo.id in :vehiculoIds"
            + " and r.estado <> com.rutaoax.recorrido.EstadoRecorrido.FINALIZADO")
    List<Recorrido> activosPorVehiculos(@Param("fecha") LocalDate fecha, @Param("vehiculoIds") Collection<Long> vehiculoIds);

    @Query("select r from Recorrido r left join fetch r.vehiculo"
            + " where r.fecha = :fecha and r.repartidor.id in :repartidorIds"
            + " and r.estado <> com.rutaoax.recorrido.EstadoRecorrido.FINALIZADO")
    List<Recorrido> activosPorRepartidores(@Param("fecha") LocalDate fecha, @Param("repartidorIds") Collection<Long> repartidorIds);

    @Query("select r.vehiculo.id from Recorrido r where r.fecha = :fecha and r.vehiculo is not null")
    List<Long> vehiculoIdsConRecorrido(@Param("fecha") LocalDate fecha);

    @Query("select r.repartidor.id from Recorrido r where r.fecha = :fecha")
    List<Long> repartidorIdsConRecorrido(@Param("fecha") LocalDate fecha);

    boolean existsByVehiculoIdAndFechaGreaterThanEqualAndEstadoNot(Long vehiculoId, LocalDate fecha, EstadoRecorrido estado);

    boolean existsByRepartidorIdAndFechaGreaterThanEqualAndEstadoNot(Long repartidorId, LocalDate fecha, EstadoRecorrido estado);

    Optional<Recorrido> findByRepartidorIdAndFecha(Long repartidorId, LocalDate fecha);

    List<Recorrido> findByVehiculoIdAndFecha(Long vehiculoId, LocalDate fecha);

    @Query("select r from Recorrido r join fetch r.repartidor rep join fetch rep.usuario left join fetch r.vehiculo where r.id = :id")
    Optional<Recorrido> detalle(@Param("id") Long id);
}
