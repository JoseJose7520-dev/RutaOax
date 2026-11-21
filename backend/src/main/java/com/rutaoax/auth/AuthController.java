package com.rutaoax.auth;

import com.rutaoax.auth.dto.ForgotPasswordRequest;
import com.rutaoax.auth.dto.LoginRequest;
import com.rutaoax.auth.dto.LoginResponse;
import com.rutaoax.auth.dto.RegisterRequest;
import com.rutaoax.auth.dto.ResetPasswordRequest;
import com.rutaoax.usuario.dto.UsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @GetMapping("/me")
    public UsuarioDTO me(@AuthenticationPrincipal UsuarioPrincipal yo) {
        return authService.me(yo.getId());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String encabezado) {
        authService.logout(encabezado.substring(7).trim());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.solicitar(req.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.restablecer(req.token(), req.password());
        return ResponseEntity.noContent().build();
    }
}
