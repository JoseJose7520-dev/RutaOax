package com.rutaoax.config;

import com.rutaoax.auth.JwtFilter;
import com.rutaoax.auth.JwtService;
import com.rutaoax.auth.TokenRevocadoRepository;
import com.rutaoax.usuario.UsuarioRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt, UsuarioRepository usuarios,
                                           TokenRevocadoRepository revocados, RestAuthenticationEntryPoint entrada,
                                           RestAccessDeniedHandler denegado) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(entrada).accessDeniedHandler(denegado))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/forgot-password",
                                "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/negocio/**", "/api/equipo/**", "/api/vehiculos/**")
                        .hasAnyRole("NEGOCIO", "ADMIN")
                        .requestMatchers("/api/disponibles/**").hasAnyRole("REPARTIDOR", "ADMIN")
                        .requestMatchers("/api/recorridos/**").hasAnyRole("NEGOCIO", "REPARTIDOR", "ADMIN")
                        .requestMatchers("/api/mensajes/**").hasAnyRole("NEGOCIO", "REPARTIDOR", "CLIENTE")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(jwt, usuarios, revocados), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> lista = Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        config.setAllowedOrigins(lista);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}
