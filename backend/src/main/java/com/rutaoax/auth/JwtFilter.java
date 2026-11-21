package com.rutaoax.auth;

import com.rutaoax.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    private final TokenRevocadoRepository revocados;

    public JwtFilter(JwtService jwt, UsuarioRepository usuarios, TokenRevocadoRepository revocados) {
        this.jwt = jwt;
        this.usuarios = usuarios;
        this.revocados = revocados;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7).trim();
            if (jwt.isValid(token) && !revocados.existsByJti(jwt.getJti(token))) {
                usuarios.findByEmail(jwt.getEmail(token)).filter(u -> u.isActivo()).ifPresent(u -> {
                    UsuarioPrincipal principal = UsuarioPrincipal.desde(u);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        chain.doFilter(request, response);
    }
}
