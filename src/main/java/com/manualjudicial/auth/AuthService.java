package com.manualjudicial.auth;

import com.manualjudicial.users.AccountStatus;
import com.manualjudicial.users.Role;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authManager;
    private final EmailService emailService;
    private final PasswordResetTokenRepository resetTokenRepository;

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        // DaoAuthenticationProvider handles BCrypt comparison here.
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Treat null accountStatus as ACTIVE (backward compat for existing rows)
        AccountStatus status = user.getAccountStatus() != null
                ? user.getAccountStatus()
                : AccountStatus.ACTIVE;

        if (status == AccountStatus.PENDING) {
            throw new RuntimeException("Tu cuenta está pendiente de aprobación. Revisa tu correo.");
        }
        if (status == AccountStatus.SUSPENDED) {
            throw new RuntimeException("Tu cuenta ha sido suspendida. Contacta al administrador.");
        }

        String token = tokenProvider.generateToken(user);
        return buildResponse(user, token);
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .accountStatus(AccountStatus.PENDING)
                .customThreshold(70)
                .build();
        userRepository.save(user);

        // Send "registration received" email (pending admin approval)
        emailService.sendRegistrationReceivedEmail(user);

        // Return response WITHOUT token (user cannot login until approved)
        return AuthResponse.builder()
                .token(null)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    // ── Password Reset – Step 1: Request ──────────────────────────────────────

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Invalidate any previous tokens for this user
            resetTokenRepository.deleteByUser(user);

            String rawToken = UUID.randomUUID().toString();
            PasswordResetToken prt = PasswordResetToken.builder()
                    .token(rawToken)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            resetTokenRepository.save(prt);

            emailService.sendPasswordResetEmail(user, rawToken);
            log.info("Password reset token created for {}", email);
        });
        // Always return silently — don't leak whether the email exists
    }

    // ── Password Reset – Step 2: Confirm ─────────────────────────────────────

    @Transactional
    public void confirmPasswordReset(String rawToken, String newPassword) {
        PasswordResetToken prt = resetTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));

        if (prt.isExpired()) {
            throw new RuntimeException("El token ha expirado. Solicita un nuevo enlace.");
        }
        if (prt.isUsed()) {
            throw new RuntimeException("El token ya fue utilizado.");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        resetTokenRepository.save(prt);

        log.info("Password successfully reset for {}", user.getEmail());
    }

    @Transactional
    public void rescueAdmin() {
        User admin = userRepository.findByEmail("admin@manual.cl")
                .orElseGet(() -> {
                    log.info("Creating admin user via rescue endpoint");
                    return User.builder()
                            .name("Administrador")
                            .email("admin@manual.cl")
                            .role(Role.ADMIN)
                            .accountStatus(AccountStatus.ACTIVE)
                            .customThreshold(70)
                            .build();
                });
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }
}
