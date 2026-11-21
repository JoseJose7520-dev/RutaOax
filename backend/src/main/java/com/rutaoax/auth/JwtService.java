package com.rutaoax.auth;

import com.rutaoax.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Usuario u) {
        Date ahora = new Date();
        List<String> roles = u.getRoles().stream().map(r -> r.getNombre().name()).sorted().toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(u.getEmail())
                .claim("uid", u.getId())
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return parse(token).getSubject();
    }

    public String getJti(String token) {
        return parse(token).getId();
    }

    public LocalDateTime getExpiration(String token) {
        return LocalDateTime.ofInstant(parse(token).getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
