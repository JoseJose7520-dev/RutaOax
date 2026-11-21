package com.rutaoax.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String hash);

    @Modifying
    @Query("update PasswordResetToken t set t.usado = true where t.usuario.id = :usuarioId and t.usado = false")
    int marcarUsadosDelUsuario(@Param("usuarioId") Long usuarioId);
}
