package com.manualjudicial.telemetry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "authorities", "password"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Chapter chapter;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "seconds_used")
    private Integer secondsUsed;
}
