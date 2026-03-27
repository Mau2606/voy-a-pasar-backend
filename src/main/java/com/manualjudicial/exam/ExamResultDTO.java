package com.manualjudicial.exam;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * DTO returned after evaluating a final exam submission.
 */
@Data
@Builder
public class ExamResultDTO {
    private int totalQuestions;
    private int correctCount;
    private double scorePercentage;
    private boolean passed;
    private List<QuestionResultDTO> results;

    @Data
    @Builder
    public static class QuestionResultDTO {
        private Long questionId;
        private String statement;
        private String optA;
        private String optB;
        private String optC;
        private String optD;
        private String optE;
        private String selectedOpt;
        private String correctOpt;
        private boolean isCorrect;
        private String explanation;
        private String pageReference;
        private String questionType;
    }
}
