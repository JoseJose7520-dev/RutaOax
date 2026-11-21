package com.rutaoax.mensaje;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    long countByDestinatarioIdAndLeidoFalse(Long destinatarioId);

    @Query("select m from Mensaje m join fetch m.remitente where m.destinatario.id = :usuarioId and m.leido = false"
            + " order by m.creadoEn desc")
    List<Mensaje> noLeidos(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
