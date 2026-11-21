package com.rutaoax.pedido;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    String FILTROS = " where (:negocioId is null or p.negocio.id = :negocioId)"
            + " and (:clienteId is null or p.cliente.id = :clienteId)"
            + " and (:repUsuarioId is null or ru.id = :repUsuarioId)"
            + " and (:q = '' or lower(p.clienteNombre) like lower(concat('%', :q, '%'))"
            + " or lower(p.direccion) like lower(concat('%', :q, '%')) or cast(p.id as string) like concat('%', :q, '%'))"
            + " and (:estado is null or p.estado = :estado)"
            + " and (:zona is null or p.zona = :zona)"
            + " and (:modalidad is null or p.modalidad = :modalidad)"
            + " and (:asig = '' or (:asig = 'SI' and rec.id is not null) or (:asig = 'NO' and rec.id is null))";

    @Query(value = "select p from Pedido p left join fetch p.negocio left join fetch p.recorrido rec"
            + " left join fetch rec.repartidor rep left join fetch rep.usuario ru" + FILTROS,
            countQuery = "select count(p) from Pedido p left join p.recorrido rec left join rec.repartidor rep"
                    + " left join rep.usuario ru" + FILTROS)
    Page<Pedido> buscar(@Param("negocioId") Long negocioId, @Param("clienteId") Long clienteId,
                        @Param("repUsuarioId") Long repUsuarioId, @Param("q") String q,
                        @Param("estado") EstadoPedido estado, @Param("zona") String zona,
                        @Param("modalidad") Modalidad modalidad, @Param("asig") String asig, Pageable pageable);

    @Query("select p from Pedido p left join fetch p.negocio left join fetch p.recorrido rec"
            + " left join fetch rec.repartidor rep left join fetch rep.usuario where p.id = :id")
    Optional<Pedido> detalle(@Param("id") Long id);

    @Query("select p from Pedido p left join fetch p.negocio left join fetch p.recorrido rec"
            + " left join fetch rec.repartidor rep left join fetch rep.usuario where p.recorrido.id = :recorridoId"
            + " order by p.ordenParada")
    List<Pedido> paradasDe(@Param("recorridoId") Long recorridoId);

    List<Pedido> findByRecorridoIdOrderByOrdenParada(Long recorridoId);

    long countByRecorridoId(Long recorridoId);

    long countByNegocioIdAndEstado(Long negocioId, EstadoPedido estado);

    long countByNegocioIdAndEstadoAndActualizadoEnGreaterThanEqual(Long negocioId, EstadoPedido estado, LocalDateTime desde);

    List<Pedido> findTop5ByNegocioIdAndEstadoOrderByActualizadoEnDesc(Long negocioId, EstadoPedido estado);

    /** Por recorrido: [recorridoId, total de pedidos, pedidos entregados]. */
    @Query("select p.recorrido.id, count(p), sum(case when p.estado = com.rutaoax.pedido.EstadoPedido.ENTREGADO then 1 else 0 end)"
            + " from Pedido p where p.recorrido.id in :ids group by p.recorrido.id")
    List<Object[]> conteoPorRecorrido(@Param("ids") Collection<Long> ids);
}
