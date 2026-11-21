package com.rutaoax.pedido;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Por recorrido: [recorridoId, total de pedidos, pedidos entregados]. */
    @Query("select p.recorrido.id, count(p), sum(case when p.estado = com.rutaoax.pedido.EstadoPedido.ENTREGADO then 1 else 0 end)"
            + " from Pedido p where p.recorrido.id in :ids group by p.recorrido.id")
    List<Object[]> conteoPorRecorrido(@Param("ids") Collection<Long> ids);
}
