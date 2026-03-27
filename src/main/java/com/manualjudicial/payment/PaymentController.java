package com.manualjudicial.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * POST /api/payments/preference
     * Creates a MercadoPago payment preference for the authenticated user.
     * Returns { preferenceId, initPoint, sandboxInitPoint }.
     */
    @PostMapping("/preference")
    public ResponseEntity<Map<String, String>> createPreference(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, String> preference = paymentService.createPreference(user.getId());
        return ResponseEntity.ok(preference);
    }

    /**
     * POST /api/payments/webhook
     * MercadoPago IPN webhook endpoint. Must be PUBLIC (no auth).
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok("OK");
    }
}
