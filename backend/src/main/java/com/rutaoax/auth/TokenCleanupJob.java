package com.rutaoax.auth;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupJob.class);

    private final TokenRevocadoRepository revocados;

    public TokenCleanupJob(TokenRevocadoRepository revocados) {
        this.revocados = revocados;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarVencidos() {
        revocados.deleteByExpiraEnBefore(LocalDateTime.now());
        log.info("Limpieza de tokens revocados vencidos completada");
    }
}
