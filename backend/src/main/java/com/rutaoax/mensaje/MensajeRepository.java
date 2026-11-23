package com.rutaoax.mensaje;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    long countByDestinatarioIdAndLeidoFalse(Long destinatarioId);

    @Query("select m from Mensaje m join fetch m.remitente where m.destinatario.id = :usuarioId and m.leido = false"
            + " order by m.creadoEn desc")
    List<Mensaje> noLeidos(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /** Una fila por conversación (pedido) en la que participo: [pedidoId, fecha del último mensaje, no leídos]. */
    @Query(value = "select m.pedido.id, max(m.creadoEn),"
            + " sum(case when m.destinatario.id = :yo and m.leido = false then 1 else 0 end)"
            + " from Mensaje m where m.remitente.id = :yo or m.destinatario.id = :yo"
            + " group by m.pedido.id order by max(m.creadoEn) desc",
            countQuery = "select count(distinct m.pedido.id) from Mensaje m"
                    + " where m.remitente.id = :yo or m.destinatario.id = :yo")
    Page<Object[]> hilos(@Param("yo") Long yo, Pageable pageable);

    /** El último mensaje de cada conversación de la página, en una sola consulta. */
    @Query("select m from Mensaje m join fetch m.remitente join fetch m.destinatario"
            + " where m.pedido.id in :pedidos and (m.remitente.id = :yo or m.destinatario.id = :yo)"
            + " and m.id = (select max(m2.id) from Mensaje m2 where m2.pedido.id = m.pedido.id"
            + " and (m2.remitente.id = :yo or m2.destinatario.id = :yo))")
    List<Mensaje> ultimos(@Param("yo") Long yo, @Param("pedidos") Collection<Long> pedidos);

    /** Mensajes de un pedido entre yo y cualquiera, del más nuevo al más viejo (la página 0 es lo más reciente). */
    @Query(value = "select m from Mensaje m join fetch m.remitente where m.pedido.id = :pedidoId"
            + " and (m.remitente.id = :yo or m.destinatario.id = :yo) order by m.creadoEn desc, m.id desc",
            countQuery = "select count(m) from Mensaje m where m.pedido.id = :pedidoId"
                    + " and (m.remitente.id = :yo or m.destinatario.id = :yo)")
    Page<Mensaje> dePedido(@Param("pedidoId") Long pedidoId, @Param("yo") Long yo, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Mensaje m set m.leido = true where m.pedido.id = :pedidoId and m.destinatario.id = :yo"
            + " and m.leido = false")
    int marcarLeidos(@Param("pedidoId") Long pedidoId, @Param("yo") Long yo);
}
