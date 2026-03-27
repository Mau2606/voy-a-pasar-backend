package com.manualjudicial.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manualjudicial.chapters.Chapter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chapter_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Chapter chapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String statement;

    @Column(name = "opt_a", nullable = false)
    private String optA;
    @Column(name = "opt_b", nullable = false)
    private String optB;
    @Column(name = "opt_c", nullable = false)
    private String optC;
    @Column(name = "opt_d", nullable = false)
    private String optD;
    @Column(name = "opt_e", nullable = false)
    private String optE;

    @Column(name = "correct_opt", nullable = false, length = 1)
    private String correctOpt; // A, B, C, D or E

    @Column(columnDefinition = "TEXT")
    private String explanation;

    /**
     * PDF page number for deep-linking. When a user reads the explanation
     * after answering this question, the PDF viewer jumps to this page.
     * Nullable — not all questions need a page reference.
     */
    @Column(name = "page_reference")
    private String pageReference;
}
