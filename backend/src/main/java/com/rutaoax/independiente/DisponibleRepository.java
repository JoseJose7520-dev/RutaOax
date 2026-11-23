package com.rutaoax.independiente;

import com.rutaoax.pedido.Pedido;
import com.rutaoax.recorrido.Recorrido;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Consultas de las entregas abiertas que puede tomar un repartidor independiente. */
public interface DisponibleRepository extends Repository<Pedido, Long> {

    /**
     * Entregas ABIERTAS, PENDIENTES y sin recorrido. La distancia se calcula en la base de datos con una
     * aproximación plana (sirve para distancias dentro de la ciudad): kx y ky son km por grado.
     */
    String FILTRO = " where p.modalidad = com.rutaoax.pedido.Modalidad.ABIERTO"
            + " and p.estado = com.rutaoax.pedido.EstadoPedido.PENDIENTE and p.recorrido is null"
            + " and (:zona is null or p.zona = :zona)"
            + " and sqrt(((p.lng - :lng0) * :kx) * ((p.lng - :lng0) * :kx)"
            + " + ((p.lat - :lat0) * :ky) * ((p.lat - :lat0) * :ky)) <= :maxKm";

    @Query(value = "select p from Pedido p left join fetch p.negocio" + FILTRO,
            countQuery = "select count(p) from Pedido p" + FILTRO)
    Page<Pedido> buscar(@Param("zona") String zona, @Param("lat0") BigDecimal lat0, @Param("lng0") BigDecimal lng0,
                        @Param("kx") BigDecimal kx, @Param("ky") BigDecimal ky, @Param("maxKm") BigDecimal maxKm,
                        Pageable pageable);

    /** Toma la entrega solo si sigue libre; si otro la tomó primero, no cambia nada y regresa 0. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Pedido p set p.estado = com.rutaoax.pedido.EstadoPedido.ASIGNADO, p.recorrido = :recorrido,"
            + " p.ordenParada = :orden where p.id = :id and p.estado = com.rutaoax.pedido.EstadoPedido.PENDIENTE"
            + " and p.recorrido is null and p.modalidad = com.rutaoax.pedido.Modalidad.ABIERTO")
    int tomarSiSigueLibre(@Param("id") Long id, @Param("recorrido") Recorrido recorrido, @Param("orden") int orden);
}
