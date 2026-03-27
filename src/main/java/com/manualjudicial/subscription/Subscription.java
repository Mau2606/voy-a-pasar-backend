package com.manualjudicial.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manualjudicial.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "authorities"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    /** Flag to track if expiry notification was already sent. */
    @Builder.Default
    @Column(name = "expiry_notified", nullable = false)
    private Boolean expiryNotified = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** True if the subscription is currently valid. */
    public boolean isCurrentlyActive() {
        return active && LocalDateTime.now().isBefore(endDate);
    }
}
