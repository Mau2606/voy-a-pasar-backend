package com.manualjudicial.payment;

import com.manualjudicial.subscription.Subscription;
import com.manualjudicial.subscription.SubscriptionService;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment service scaffolded for MercadoPago integration.
 *
 * To activate, add the mercadopago-sdk dependency to pom.xml and
 * set the real ACCESS_TOKEN in application.yml.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    @Value("${app.mercadopago.access-token:TEST-ACCESS-TOKEN}")
    private String accessToken;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private static final BigDecimal PREMIUM_PRICE = new BigDecimal("9990");

    /**
     * Creates a MercadoPago payment preference for premium subscription.
     * Returns a map with the preference data (init_point URL, preferenceId).
     *
     * NOTE: This is scaffolded. Replace the body with actual MercadoPago SDK calls
     * when credentials are available. Example:
     *
     * <pre>
     * MercadoPagoConfig.setAccessToken(accessToken);
     * PreferenceClient client = new PreferenceClient();
     * PreferenceItemRequest item = PreferenceItemRequest.builder()
     *     .title("Suscripción Premium Voy a Pasar - 1 Mes")
     *     .quantity(1)
     *     .unitPrice(PREMIUM_PRICE)
     *     .currencyId("CLP")
     *     .build();
     * PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
     *     .success(frontendUrl + "/payment/success")
     *     .failure(frontendUrl + "/payment/failure")
     *     .pending(frontendUrl + "/payment/pending")
     *     .build();
     * PreferenceRequest request = PreferenceRequest.builder()
     *     .items(List.of(item))
     *     .backUrls(backUrls)
     *     .externalReference(userId.toString())
     *     .notificationUrl(backendUrl + "/api/payments/webhook")
     *     .build();
     * Preference preference = client.create(request);
     * </pre>
     */
    public Map<String, String> createPreference(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        log.info("Creating payment preference for user: {}", user.getEmail());

        // Scaffold response — replace with real MercadoPago SDK call
        Map<String, String> response = new HashMap<>();
        response.put("preferenceId", "SCAFFOLD-PREF-" + userId);
        response.put("initPoint", "https://www.mercadopago.cl/checkout/v1/redirect?pref_id=SCAFFOLD-PREF-" + userId);
        response.put("sandboxInitPoint", "https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=SCAFFOLD-PREF-" + userId);
        return response;
    }

    /**
     * Handles the MercadoPago IPN webhook notification.
     * Validates the payment, creates a Payment record, and activates the subscription.
     */
    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        String type = (String) payload.get("type");
        if (!"payment".equals(type)) {
            log.debug("Ignoring non-payment webhook type: {}", type);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) return;

        String paymentId = String.valueOf(data.get("id"));
        log.info("Processing MercadoPago payment webhook: {}", paymentId);

        // Check for duplicate
        if (paymentRepository.findByExternalPaymentId(paymentId).isPresent()) {
            log.info("Payment {} already processed, skipping", paymentId);
            return;
        }

        /*
         * In production, you would call MercadoPago API to verify the payment:
         *
         * PaymentClient paymentClient = new PaymentClient();
         * com.mercadopago.resources.payment.Payment mpPayment = paymentClient.get(Long.parseLong(paymentId));
         * String status = mpPayment.getStatus(); // "approved", "pending", "rejected"
         * Long userId = Long.parseLong(mpPayment.getExternalReference());
         *
         * For now, we scaffold assuming approved:
         */
        String externalReference = String.valueOf(payload.getOrDefault("external_reference", "0"));
        Long userId;
        try {
            userId = Long.parseLong(externalReference);
        } catch (NumberFormatException e) {
            log.error("Invalid external_reference in webhook: {}", externalReference);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User not found for payment webhook, userId: {}", userId);
            return;
        }

        // Activate premium subscription
        Subscription subscription = subscriptionService.activatePremium(userId);

        // Create payment record
        Payment payment = Payment.builder()
                .user(user)
                .subscription(subscription)
                .externalPaymentId(paymentId)
                .amount(PREMIUM_PRICE)
                .currency("CLP")
                .status(PaymentStatus.APPROVED)
                .build();
        paymentRepository.save(payment);

        log.info("Payment {} processed, premium activated for user {}", paymentId, user.getEmail());
    }
}
