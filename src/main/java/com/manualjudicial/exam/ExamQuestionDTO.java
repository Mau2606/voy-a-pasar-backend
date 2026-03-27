package com.manualjudicial.exam;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for sending exam questions to the frontend WITHOUT the correct answer
 * and explanation (to prevent cheating in exam mode).
 */
@Data
@Builder
public class ExamQuestionDTO {
    private Long id;
    private String statement;
    private String optA;
    private String optB;
    private String optC;
    private String optD;
    private String optE;
    private String questionType;
    private Long chapterId;
    private String chapterTitle;
}
