package com.manualjudicial.subscription;

import com.manualjudicial.auth.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that runs daily at 9 AM to check for expired subscriptions
 * and send notification emails.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryJob {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *") // Every day at 9:00 AM
    @Transactional
    public void checkExpiredSubscriptions() {
        log.info("Running subscription expiry check...");
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expired = subscriptionRepository.findExpiredNotNotified(now);

        for (Subscription sub : expired) {
            try {
                emailService.sendSubscriptionExpiryEmail(sub.getUser(), sub.getEndDate());
                sub.setExpiryNotified(true);
                sub.setActive(false);
                subscriptionRepository.save(sub);
                log.info("Expiry notification sent to: {}", sub.getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to process expiry for subscription {}: {}", sub.getId(), e.getMessage());
            }
        }

        log.info("Subscription expiry check complete. Processed {} subscriptions.", expired.size());
    }
}
