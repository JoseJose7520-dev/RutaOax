package com.rutaoax.notificacion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacionLogRepository extends JpaRepository<NotificacionLog, Long> {

    @Query("select n from NotificacionLog n where (:canal is null or n.canal = :canal)"
            + " and (:estado is null or n.estado = :estado)")
    Page<NotificacionLog> buscar(@Param("canal") Canal canal, @Param("estado") EstadoEnvio estado, Pageable pageable);
}
