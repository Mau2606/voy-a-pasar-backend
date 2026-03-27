package com.manualjudicial.telemetry;

import lombok.Data;

@Data
public class QuestionTimeDTO {
    private Long questionId;
    private Long chapterId;
    private Long answerTimeMs;
    private Boolean isCorrect;
}
