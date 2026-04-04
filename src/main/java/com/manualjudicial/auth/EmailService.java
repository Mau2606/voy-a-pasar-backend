package com.manualjudicial.auth;

import com.manualjudicial.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Transactional email service for the "Voy a Pasar" platform.
 * Uses the Resend HTTP API (https://resend.com/docs/api-reference/emails/send-email)
 * via RestTemplate — no external SDK needed.
 */
@Service
@Slf4j
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${app.resend.api-key:}")
    private String apiKey;

    @Value("${app.resend.from-email:}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─────────────────────────────────────────────────────────────────────────────
    // Configuration check
    // ─────────────────────────────────────────────────────────────────────────────

    private boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("re_123456789")
                && fromEmail != null && !fromEmail.isBlank();
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  1) REGISTRO RECIBIDO
    // ═════════════════════════════════════════════════════════════════════════════

    /** Sent after user registers — account is pending admin review. */
    @Async
    public void sendRegistrationReceivedEmail(User user) {
        sendRegistrationReceivedEmail(user.getEmail(), user.getName());
    }

    public void sendRegistrationReceivedEmail(String toEmail, String userName) {
        String subject = "Solicitud de incorporación recibida - Voy a pasar";
        String html = wrapHtmlTemplate(
            "Solicitud Recibida",
            "👋 ¡Hola " + esc(userName) + "!",
            "<p>Hemos recibido tu <strong>solicitud de incorporación</strong> a la plataforma " +
            "<strong>Voy a Pasar</strong>.</p>" +
            "<p>Tu cuenta se encuentra actualmente en <strong>estado de revisión</strong>. " +
            "Nuestro equipo evaluará tu solicitud a la brevedad.</p>" +
            infoBox(
                "📋 ¿Qué sigue?",
                "Recibirás un correo electrónico con la <strong>aprobación</strong> de tu cuenta " +
                "y los <strong>pasos a seguir</strong> para comenzar a estudiar."
            ) +
            "<p style=\"margin-top:24px;color:#6b7280;\">Agradecemos tu paciencia y confianza.</p>"
        );
        send(toEmail, subject, html);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  2) SOLICITUD APROBADA + INSTRUCCIONES DE PAGO
    // ═════════════════════════════════════════════════════════════════════════════

    public void sendPaymentInstructionsEmail(String toEmail, String userName, String accessType) {
        String subject = "¡Solicitud Aprobada! Instrucciones de pago - Voy a pasar";
        String planLabel = accessType != null ? accessType.replace("_", " ") : "Estándar";
        String html = wrapHtmlTemplate(
            "¡Solicitud Aprobada!",
            "🎉 ¡Felicidades " + esc(userName) + "!",
            "<p>Tu solicitud de incorporación ha sido <strong style=\"color:#059669;\">aprobada</strong>. " +
            "Estás a un paso de comenzar tu preparación con el plan <strong>" + esc(planLabel) + "</strong>.</p>" +
            "<p>Para activar tu acceso completo, realiza el pago mediante transferencia bancaria " +
            "a los siguientes datos:</p>" +
            paymentBox() +
            "<p style=\"margin-top:20px;\">Una vez realizada la transferencia:</p>" +
            "<ol style=\"line-height:2;\">" +
            "  <li>📸 Envía tu <strong>comprobante de pago</strong> respondiendo a este correo.</li>" +
            "  <li>📱 O envíalo por <strong>WhatsApp</strong> al número indicado en la plataforma.</li>" +
            "</ol>" +
            infoBox(
                "⚡ Activación rápida",
                "Tu acceso será habilitado dentro de las próximas <strong>24 horas hábiles</strong> " +
                "tras verificar tu pago."
            )
        );
        send(toEmail, subject, html);
    }

    /** Convenience overload when User entity is available. */
    @Async
    public void sendAccountApprovedEmail(User user) {
        String accessType = user.getAccessType() != null ? user.getAccessType().name() : "STANDARD";
        sendPaymentInstructionsEmail(user.getEmail(), user.getName(), accessType);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  3) SUSCRIPCIÓN EXPIRADA
    // ═════════════════════════════════════════════════════════════════════════════

    public void sendSubscriptionExpiredEmail(String toEmail, String userName) {
        String subject = "Tu acceso a los manuales ha expirado - Voy a pasar";
        String html = wrapHtmlTemplate(
            "Acceso Expirado",
            "⏰ Hola " + esc(userName),
            "<p>Te informamos que tu <strong>membresía</strong> en la plataforma " +
            "<strong>Voy a Pasar</strong> ha finalizado.</p>" +
            "<p>Mientras tanto, puedes seguir accediendo al contenido gratuito disponible.</p>" +
            infoBox(
                "🔄 ¿Quieres renovar?",
                "Puedes renovar tu acceso en cualquier momento contactándote con la administración " +
                "respondiendo a este correo o a través de nuestra plataforma."
            ) +
            ctaButton("Ir a la plataforma", frontendUrl)
        );
        send(toEmail, subject, html);
    }

    /** Backward-compatible overload used by SubscriptionExpiryJob. */
    @Async
    public void sendSubscriptionExpiryEmail(User user, LocalDateTime expiryDate) {
        sendSubscriptionExpiredEmail(user.getEmail(), user.getName());
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  PASSWORD RESET
    // ═════════════════════════════════════════════════════════════════════════════

    @Async
    public void sendPasswordResetEmail(User user, String rawToken) {
        String link = frontendUrl + "/reset-password?token=" + rawToken;
        String subject = "Recuperación de contraseña – Voy a Pasar";
        String html = wrapHtmlTemplate(
            "Recuperar Contraseña",
            "🔐 Hola " + esc(user.getName()),
            "<p>Recibimos una solicitud para restablecer tu contraseña.</p>" +
            "<p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>" +
            ctaButton("Restablecer contraseña", link) +
            "<p style=\"margin-top:20px;color:#6b7280;font-size:13px;\">" +
            "Este enlace es válido por <strong>1 hora</strong>. Si no solicitaste este cambio, " +
            "puedes ignorar este correo.</p>"
        );
        send(user.getEmail(), subject, html);
    }

    // ── Legacy Compat ─────────────────────────────────────────────────────────

    /** @deprecated Use {@link #sendRegistrationReceivedEmail(User)} instead. */
    @Deprecated
    public void sendWelcomeEmail(User user) {
        sendRegistrationReceivedEmail(user);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  RESEND API CALL
    // ═════════════════════════════════════════════════════════════════════════════

    private void send(String to, String subject, String htmlBody) {
        if (!isConfigured()) {
            log.info("[EMAIL-SKIPPED] {} → {} (Resend not configured)", subject, to);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", new String[]{to},
                "subject", subject,
                "html", htmlBody
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[EMAIL-SENT] {} → {}", subject, to);
            } else {
                log.warn("[EMAIL-FAIL] {} → {} — Status: {}", subject, to, response.getStatusCode());
            }
        } catch (Exception ex) {
            log.error("[EMAIL-ERROR] {} → {}: {}", subject, to, ex.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  HTML TEMPLATE HELPERS
    // ═════════════════════════════════════════════════════════════════════════════

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String wrapHtmlTemplate(String preheader, String heading, String content) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>%s</title>
              <style>
                body { margin:0; padding:0; background:#f1f5f9; font-family:'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif; }
                .container { max-width:600px; margin:0 auto; padding:40px 20px; }
                .card { background:#ffffff; border-radius:16px; box-shadow:0 4px 24px rgba(0,0,0,.06); overflow:hidden; }
                .header { background:linear-gradient(135deg,#7c3aed 0%%,#4f46e5 100%%); padding:32px 40px; text-align:center; }
                .header h1 { margin:0; color:#ffffff; font-size:22px; font-weight:700; }
                .header p { margin:4px 0 0; color:#e0d4ff; font-size:13px; }
                .body { padding:32px 40px; color:#374151; font-size:15px; line-height:1.7; }
                .body h2 { margin:0 0 16px; color:#1f2937; font-size:20px; }
                .footer { padding:24px 40px; background:#f8fafc; text-align:center; border-top:1px solid #e2e8f0; }
                .footer p { margin:0; color:#9ca3af; font-size:12px; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="card">
                  <div class="header">
                    <h1>🎓 Voy a Pasar</h1>
                    <p>Plataforma de Estudio</p>
                  </div>
                  <div class="body">
                    <h2>%s</h2>
                    %s
                  </div>
                  <div class="footer">
                    <p>© 2026 Voy a Pasar — Todos los derechos reservados</p>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(preheader, heading, content);
    }

    private static String infoBox(String title, String text) {
        return """
            <div style="margin:20px 0;padding:16px 20px;background:#eff6ff;border-left:4px solid #3b82f6;border-radius:8px;">
              <p style="margin:0 0 6px;font-weight:700;color:#1e40af;font-size:14px;">%s</p>
              <p style="margin:0;color:#1e3a5f;font-size:14px;">%s</p>
            </div>
            """.formatted(title, text);
    }

    private static String paymentBox() {
        return """
            <div style="margin:20px 0;padding:20px 24px;background:linear-gradient(135deg,#fefce8,#fef9c3);border:2px solid #facc15;border-radius:12px;">
              <p style="margin:0 0 12px;font-weight:700;color:#854d0e;font-size:16px;">💳 Datos de Transferencia Bancaria</p>
              <table style="width:100%%;font-size:14px;color:#78350f;border-collapse:collapse;">
                <tr><td style="padding:6px 0;font-weight:600;">Banco:</td><td style="padding:6px 0;">[NOMBRE_BANCO]</td></tr>
                <tr><td style="padding:6px 0;font-weight:600;">Tipo cuenta:</td><td style="padding:6px 0;">[TIPO_CUENTA]</td></tr>
                <tr><td style="padding:6px 0;font-weight:600;">N° Cuenta:</td><td style="padding:6px 0;">[NUMERO_CUENTA]</td></tr>
                <tr><td style="padding:6px 0;font-weight:600;">RUT:</td><td style="padding:6px 0;">[RUT]</td></tr>
                <tr><td style="padding:6px 0;font-weight:600;">Nombre:</td><td style="padding:6px 0;">[NOMBRE_TITULAR]</td></tr>
                <tr><td style="padding:6px 0;font-weight:600;">Email:</td><td style="padding:6px 0;">[EMAIL_TITULAR]</td></tr>
              </table>
            </div>
            """;
    }

    private static String ctaButton(String text, String url) {
        return """
            <div style="text-align:center;margin:28px 0;">
              <a href="%s" style="display:inline-block;padding:14px 32px;background:linear-gradient(135deg,#7c3aed,#4f46e5);color:#ffffff;font-size:15px;font-weight:700;text-decoration:none;border-radius:12px;box-shadow:0 4px 14px rgba(124,58,237,.3);">
                %s
              </a>
            </div>
            """.formatted(url, text);
    }
}
