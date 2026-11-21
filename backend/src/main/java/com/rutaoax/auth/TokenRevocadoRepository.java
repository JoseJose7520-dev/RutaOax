package com.rutaoax.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, Long> {

    boolean existsByJti(String jti);

    void deleteByExpiraEnBefore(LocalDateTime fecha);
}
