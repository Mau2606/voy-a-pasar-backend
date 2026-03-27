package com.manualjudicial.progress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "authorities" })
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chapter_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Chapter chapter;

    @Column(nullable = false)
    private Double scorePercentage;

    /** Number of correct answers in this session. */
    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    /** Total questions answered in this session. */
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    /**
     * True when scorePercentage >= user's customThreshold at the time of the
     * attempt.
     * Cached here so we don't need to re-evaluate historical threshold changes.
     */
    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed;

    @Column(nullable = false)
    private LocalDateTime attemptDate;
}
