package com.manualjudicial.telemetry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manualjudicial.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_session_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "authorities"})
    private User user;

    @Column(name = "session_start", nullable = false)
    private LocalDateTime sessionStart;

    @Column(name = "session_end")
    private LocalDateTime sessionEnd;

    /** Total active session time in milliseconds. */
    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
