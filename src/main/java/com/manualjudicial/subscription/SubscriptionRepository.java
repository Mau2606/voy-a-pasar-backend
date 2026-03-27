package com.manualjudicial.subscription;

import com.manualjudicial.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByUserAndActiveTrueOrderByEndDateDesc(User user);

    @Query("SELECT s FROM Subscription s WHERE s.active = true AND s.endDate <= :now AND s.expiryNotified = false")
    List<Subscription> findExpiredNotNotified(LocalDateTime now);

    List<Subscription> findByUserOrderByCreatedAtDesc(User user);
}
