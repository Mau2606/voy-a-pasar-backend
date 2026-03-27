package com.manualjudicial.exam;

import lombok.Data;
import java.util.List;

/**
 * DTO for submitting a completed final exam.
 */
@Data
public class ExamSubmissionDTO {
    private Long manualId;
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        private Long questionId;
        private String selectedOpt;
    }
}
