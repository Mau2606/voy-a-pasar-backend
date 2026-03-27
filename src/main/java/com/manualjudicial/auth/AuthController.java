package com.manualjudicial.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Existing endpoints ────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // ── Password Reset ────────────────────────────────────────────────────────

    /**
     * Step 1 – Request a reset link.
     * Always returns 200 OK to avoid leaking whether an email exists.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message",
                "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña."));
    }

    /**
     * Step 2 – Confirm token and set new password.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
    }

    /**
     * Emergency Rescue – Resets admin@manual.cl password to admin123.
     */
    @PostMapping("/rescue-admin")
    public ResponseEntity<Map<String, String>> rescueAdmin() {
        authService.rescueAdmin();
        return ResponseEntity.ok(Map.of("message", "Admin password reset to: admin123"));
    }
}
