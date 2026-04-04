package com.manualjudicial.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RecaptchaService recaptchaService;

    // ── Existing endpoints ────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        
        // Securely set the JWT as an HttpOnly cookie
        ResponseCookie tokenCookie = ResponseCookie.from("auth_token", authResponse.getToken())
                .httpOnly(true)
                .secure(false) // Set to true if using HTTPS in prod
                .path("/")
                .maxAge(3 * 60 * 60) // 3 hours
                .sameSite("Lax")
                .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());
        
        // Remove token from JSON body for extra safety
        authResponse.setToken(null);
        
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (!recaptchaService.verify(request.getCaptchaToken())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Verificación CAPTCHA inválida. Inténtalo de nuevo."));
        }
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return ResponseEntity.ok(AuthResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .accessType(user.getAccessType() != null ? user.getAccessType().name() : null)
                .expirationDate(user.getExpirationDate() != null ? user.getExpirationDate().toString() : null)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ── Password Reset ────────────────────────────────────────────────────────

    /**
     * Step 1 – Request a reset link.
     * Always returns 200 OK to avoid leaking whether an email exists.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        if (!recaptchaService.verify(request.getCaptchaToken())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Verificación CAPTCHA inválida. Inténtalo de nuevo."));
        }
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
     * Update password for an authenticated user.
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
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
