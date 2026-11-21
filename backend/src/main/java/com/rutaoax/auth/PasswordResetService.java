package com.rutaoax.auth;

import com.rutaoax.common.ConflictException;
import com.rutaoax.notificacion.EmailService;
import com.rutaoax.usuario.Usuario;
import com.rutaoax.usuario.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int MINUTOS_VIGENCIA = 30;
    private static final String ENLACE_INVALIDO = "El enlace no es válido o ya venció";

    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final String frontendUrl;
    private final boolean registrarEnlace;
    private final SecureRandom azar = new SecureRandom();

    public PasswordResetService(UsuarioRepository usuarios, PasswordResetTokenRepository tokens,
                                PasswordEncoder encoder, EmailService emailService,
                                @Value("${app.frontend-url}") String frontendUrl,
                                @Value("${app.dev.log-reset-link:false}") boolean registrarEnlace) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.encoder = encoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.registrarEnlace = registrarEnlace;
    }

    @Transactional
    public void solicitar(String emailEntrada) {
        String correo = emailEntrada == null ? "" : emailEntrada.trim().toLowerCase();
        Usuario u = usuarios.findByEmail(correo).filter(x -> x.isActivo()).orElse(null);
        if (u == null) {
            return;
        }
        tokens.marcarUsadosDelUsuario(u.getId());

        String token = generarToken();
        PasswordResetToken registro = new PasswordResetToken();
        registro.setUsuario(u);
        registro.setTokenHash(hash(token));
        registro.setExpiraEn(LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA));
        registro.setUsado(false);
        tokens.save(registro);

        String enlace = frontendUrl + "/restablecer?token=" + token;
        if (registrarEnlace) {
            log.info("[DESARROLLO] Enlace de recuperación para {}: {}", u.getEmail(), enlace);
        }
        emailService.enviar(u.getEmail(), "Recupera tu contraseña de RutaOax", cuerpo(u.getNombre(), enlace),
                "RECUPERACION", u.getId());
    }

    @Transactional
    public void restablecer(String token, String nuevaClave) {
        PasswordResetToken registro = tokens.findByTokenHash(hash(token))
                .orElseThrow(() -> new ConflictException(ENLACE_INVALIDO));
        if (registro.isUsado() || registro.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new ConflictException(ENLACE_INVALIDO);
        }
        Usuario u = registro.getUsuario();
        u.setPasswordHash(encoder.encode(nuevaClave));
        usuarios.save(u);
        registro.setUsado(true);
        tokens.save(registro);
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        azar.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private String cuerpo(String nombre, String enlace) {
        return "<div style=\"font-family:Arial,sans-serif;color:#15171e\">"
                + "<h2>Hola, " + nombre + "</h2>"
                + "<p>Recibimos una solicitud para restablecer tu contraseña de RutaOax.</p>"
                + "<p><a href=\"" + enlace + "\" style=\"background:#2f5bd0;color:#fff;padding:10px 18px;"
                + "border-radius:8px;text-decoration:none\">Crear una contraseña nueva</a></p>"
                + "<p>El enlace vence en " + MINUTOS_VIGENCIA + " minutos y solo se puede usar una vez.</p>"
                + "<p>Si no fuiste tú, ignora este correo.</p></div>";
    }
}
