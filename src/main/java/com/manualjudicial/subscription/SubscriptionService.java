package com.manualjudicial.subscription;

import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Check if a user currently has an active PREMIUM subscription.
     */
    public boolean hasActivePremium(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return subscriptionRepository.findTopByUserAndActiveTrueOrderByEndDateDesc(user)
                .map(Subscription::isCurrentlyActive)
                .orElse(false);
    }

    public boolean hasActivePremiumByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return hasActivePremium(user.getId());
    }

    /**
     * Activate a 1-month PREMIUM subscription for a user.
     */
    @Transactional
    public Subscription activatePremium(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Deactivate any existing active subscriptions
        subscriptionRepository.findTopByUserAndActiveTrueOrderByEndDateDesc(user)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    subscriptionRepository.save(existing);
                });

        LocalDateTime now = LocalDateTime.now();
        Subscription sub = Subscription.builder()
                .user(user)
                .plan(SubscriptionPlan.PREMIUM)
                .startDate(now)
                .endDate(now.plusMonths(1))
                .active(true)
                .build();

        log.info("Activated PREMIUM subscription for user {} until {}", user.getEmail(), sub.getEndDate());
        return subscriptionRepository.save(sub);
    }
}
