package com.manualjudicial.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Validates Google reCAPTCHA v2 tokens against Google's siteverify API.
 */
@Service
@Slf4j
public class RecaptchaService {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Value("${app.recaptcha.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verifies a reCAPTCHA token with Google.
     * @param token The g-recaptcha-response token from the frontend.
     * @return true if the token is valid, false otherwise.
     */
    public boolean verify(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.postForObject(VERIFY_URL, request, Map.class);

            if (body != null && Boolean.TRUE.equals(body.get("success"))) {
                return true;
            }

            log.warn("reCAPTCHA verification failed: {}", body);
            return false;
        } catch (Exception e) {
            log.error("reCAPTCHA verification error", e);
            return false;
        }
    }
}
