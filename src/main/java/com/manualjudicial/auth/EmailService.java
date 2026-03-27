package com.manualjudicial.auth;

import com.manualjudicial.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email service for the Voy a Pasar platform.
 * JavaMailSender is injected with required=false so the app starts even when
 * SMTP credentials are not configured.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private boolean isConfigured() {
        return mailSender != null
                && fromAddress != null
                && !fromAddress.isBlank()
                && !fromAddress.equals("YOUR_GMAIL_ADDRESS");
    }

    // ── Registration Flow ─────────────────────────────────────────────────────

    /** Sent after registration — account is pending admin approval. */
    public void sendRegistrationReceivedEmail(User user) {
        if (!isConfigured()) {
            log.info("[EMAIL-SKIPPED] Registration received → {}", user.getEmail());
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(user.getEmail());
        msg.setSubject("Tu solicitud fue recibida – Voy a Pasar 🎓");
        msg.setText(String.format(
                "Hola %s,\n\n" +
                "Hemos recibido tu solicitud de registro en Voy a Pasar.\n\n" +
                "Tu cuenta está pendiente de aprobación por parte del administrador. " +
                "Te enviaremos un correo cuando tu cuenta esté activa.\n\n" +
                "¡Gracias por tu paciencia!\n\n" +
                "Equipo Voy a Pasar",
                user.getName()));
        trySend(msg, user.getEmail());
    }

    /** Sent when admin approves the account. */
    public void sendAccountApprovedEmail(User user) {
        if (!isConfigured()) {
            log.info("[EMAIL-SKIPPED] Account approved → {}", user.getEmail());
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(user.getEmail());
        msg.setSubject("¡Tu cuenta está activa! – Voy a Pasar ✅");
        msg.setText(String.format(
                "Hola %s,\n\n" +
                "¡Buenas noticias! Tu cuenta en Voy a Pasar ha sido aprobada.\n\n" +
                "Ya puedes ingresar y comenzar a estudiar: %s\n\n" +
                "¡Mucho éxito en tu preparación!\n\n" +
                "Equipo Voy a Pasar",
                user.getName(), frontendUrl));
        trySend(msg, user.getEmail());
    }

    // ── Subscription ──────────────────────────────────────────────────────────

    /** Sent when a premium subscription expires. */
    public void sendSubscriptionExpiryEmail(User user, LocalDateTime expiryDate) {
        if (!isConfigured()) {
            log.info("[EMAIL-SKIPPED] Subscription expired → {}", user.getEmail());
            return;
        }
        String formattedDate = expiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(user.getEmail());
        msg.setSubject("Tu suscripción Premium ha expirado – Voy a Pasar");
        msg.setText(String.format(
                "Hola %s,\n\n" +
                "Tu suscripción Premium de Voy a Pasar expiró el %s.\n\n" +
                "Renueva tu suscripción para seguir accediendo a todo el contenido:\n%s\n\n" +
                "Equipo Voy a Pasar",
                user.getName(), formattedDate, frontendUrl));
        trySend(msg, user.getEmail());
    }

    // ── Password Reset ────────────────────────────────────────────────────────

    /** Password-reset link email. */
    public void sendPasswordResetEmail(User user, String rawToken) {
        String link = frontendUrl + "/reset-password?token=" + rawToken;
        if (!isConfigured()) {
            log.info("[EMAIL-SKIPPED] Reset link for {}: {}", user.getEmail(), link);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(user.getEmail());
        msg.setSubject("Recuperación de contraseña – Voy a Pasar");
        msg.setText(String.format(
                "Hola %s,\n\nRestablece tu contraseña (válido 1 hora):\n%s\n\nEquipo Voy a Pasar",
                user.getName(), link));
        trySend(msg, user.getEmail());
    }

    // ── Legacy (kept for backward compatibility) ──────────────────────────────

    /** @deprecated Use {@link #sendRegistrationReceivedEmail(User)} instead. */
    @Deprecated
    public void sendWelcomeEmail(User user) {
        sendRegistrationReceivedEmail(user);
    }

    private void trySend(SimpleMailMessage msg, String to) {
        try {
            mailSender.send(msg);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
